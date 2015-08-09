package com.xun360.guojin.dataexchange.conductor;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;

import org.apache.log4j.Logger;

import com.xun360.guojin.configure.bean.RiskComponent;
import com.xun360.guojin.dataexchange.cfg.ExchangeConfig;
import com.xun360.guojin.dataexchange.collector.AbstractTradingCollector;
import com.xun360.guojin.dataexchange.collector.AccountMoneyCollector;
import com.xun360.guojin.dataexchange.collector.AccountPositionCollector;
import com.xun360.guojin.dataexchange.collector.AssistDataCache;
import com.xun360.guojin.dataexchange.collector.CollectedDataCache;
import com.xun360.guojin.dataexchange.collector.InstrumentCollector;
import com.xun360.guojin.dataexchange.collector.MarketCollector;
import com.xun360.guojin.dataexchange.exception.CollectDataException;
import com.xun360.guojin.dataexchange.factory.TraderApiEntryFactory;
import com.xun360.guojin.dataexchange.model.AccountDetail;

public class CollectJobConductor {

	private static Logger log = Logger.getLogger(CollectJobConductor.class);

	private static ConcurrentMap<AccountDetail, AccountMoneyCollector> AccountMoneyCollectorCache = new ConcurrentHashMap<AccountDetail, AccountMoneyCollector>();
	private static ConcurrentMap<AccountDetail, AccountPositionCollector> AccountPositionCollectorCache = new ConcurrentHashMap<AccountDetail, AccountPositionCollector>();

	// /**-------线程池---------**/
	private final static ExecutorService threadPool = Executors
			.newFixedThreadPool(5);

	/** -------线程池执行钩子--------- **/
	/** -------账户资金--------- **/
	private volatile static Future<?> marketDataResult;
	/** -------账户资金--------- **/
	/** -------账户持仓--------- **/
	private volatile static Future<?> traderCollectDataResult;

	private static volatile Object metux = 0;
	
	private static Semaphore tradingKillSemaphore = new Semaphore(1);
	
	private static Thread riskControlThread = null;
	
	public synchronized static void initBaseData() throws CollectDataException{
		//重置工厂的所有缓存
		TraderApiEntryFactory.resetAllCacheAPI();
		//重置Collector实例缓存
		resetFailTimesCollectorCache();
		//----
		CollectedDataCache.instrumentsMap.clear();
		CollectedDataCache.accountPositionDataMap.clear();
		CollectedDataCache.accountMoneyDataMap.clear();
		CollectedDataCache.instrumentsMarketDataMap.clear();
		//----
		// 初始化合约列表
		log.info("初始化合约列表");
		try {
			new InstrumentCollector().collect();
		} catch (CollectDataException e) {
			String message = "启动数据交换任务失败，原因合约列表初始化失败," + e.getMessage();
			log.error(message);
			throw new CollectDataException(message);
		}
		if (CollectedDataCache.instrumentsMap.isEmpty()) {
			String message = "启动数据交换任务失败，合约列表为空";
			log.error(message);
			throw new CollectDataException(message);
		}
		// 初始化账户列表
		log.info("初始化所有账户列表");
		AssistDataCache.initClient();
		log.info("账户共" + AssistDataCache.accountMap.size() + "个");
		
		//----初始化所有账户持仓
		log.info("初始化所有账户的持仓信息");
		for (AccountDetail accountDetail : AssistDataCache.accountMap.values()) {
			log.info("初始化账户【" + accountDetail.getUserId() + "】的持仓信息");
			try {
				getAccountPositionCollectorFromCache(accountDetail).collect();
			} catch (CollectDataException e) {
				String message = "初始化账户【" + accountDetail.getUserId()
						+ "】的持仓信息失败，原因：" + e.getMessage();
				log.error(message);
			}
		}
		log.info("初始化所有账户的持仓信息……成功，获取持仓账户"
				+ CollectedDataCache.accountPositionDataMap.size() + "个");
		// 初始化所有账户资金
		log.info("初始化所有账户的资金信息");
		for (AccountDetail accountDetail : AssistDataCache.accountMap.values()) {
			log.info("初始化账户【" + accountDetail.getUserId() + "】的资金信息");
			try {
				getAccountMoneyCollectorFromCache(accountDetail).collect();
			} catch (CollectDataException e) {
				String message = "初始化账户【" + accountDetail.getUserId()
						+ "】的持仓信息失败，原因：" + e.getMessage();
				log.error(message);
			}
		}
		log.info("初始化所有账户的资金信息……成功，获取资金账户"
				+ CollectedDataCache.accountMoneyDataMap.size() + "个");
	}
	
	public synchronized static void startCollect() throws CollectDataException {
		//初始化基础数据
		initBaseData();
		//重置工厂的所有缓存
		TraderApiEntryFactory.resetAllCacheAPI();
		//重置Collector实例缓存
		resetFailTimesCollectorCache();
		// 开始循环收取信息
		startMarketDataCollectJob();
		startTradingCollectJob();
		startRiskControl();
	}
	
	public synchronized static void stopCollect(){
		shutdownMarketDataCollectJob();
		shutdownTradingCollectJob();
		shutdownRiskControl();
	}
	
	/**
	 * 开始执行风控
	 */
	public synchronized static void startRiskControl(){
		if(ExchangeConfig.RISK_CONTROL && (riskControlThread == null || !riskControlThread.isAlive())){
			riskControlThread = new Thread(new Runnable() {
				public void run() {
					while (!Thread.currentThread().isInterrupted()) {
						RiskComponent.computeAllInvestorRisk();
						try {
							Thread.sleep(2000);
						} catch (InterruptedException e) {
							Thread.currentThread().interrupt();
						}
					}
				}
			});
			riskControlThread.start();
		}
	}
	
	public synchronized static void shutdownRiskControl(){
		if(ExchangeConfig.RISK_CONTROL && riskControlThread != null && !riskControlThread.isInterrupted()){
			riskControlThread.interrupt();
		}
	}

	/**
	 * 获取账户行情的调度
	 */
	public static void startTradingCollectJob() {
		synchronized (metux) {
			if (traderCollectDataResult != null && !traderCollectDataResult.isCancelled() && !traderCollectDataResult.isDone()) {
				log.warn("【交易相关数据获取】任务……已经处于启动状态");
				return;
			}
			Runnable oprs = new Runnable() {
				public void run() {
					while (!Thread.currentThread().isInterrupted()) {
						try {
							tradingKillSemaphore.acquire();//不能杀啦
						} catch (InterruptedException e) {
							Thread.currentThread().interrupt();
						}
						final List<AccountDetail> accountDetails = new ArrayList<AccountDetail>(
								AssistDataCache.accountMap.values());
						log.info("尝试……执行一轮【交易相关数据获取】操作……启动");
						for (AccountDetail accountDetail : accountDetails) {
							try {
								// 资金
								getAccountMoneyCollectorFromCache(accountDetail)
										.collect();
								// 持仓
								getAccountPositionCollectorFromCache(
										accountDetail).collect();
							}catch(CollectDataException e){
							}catch (Exception e) {
								//就算报错也不能终结
							}
						}
						log.info("尝试……执行一轮【交易相关数据获取】操作……成功");
						try {
							tradingKillSemaphore.release();//来杀吧
							Thread.sleep(ExchangeConfig.DATA_UPDATE_DURATION);
						} catch (InterruptedException e) {
							Thread.currentThread().interrupt();
						}
					}
					log.warn("有其它线程关闭了……【交易相关数据获取】任务……当前任务终止");
				}
			};
			log.info("尝试启动……【交易相关数据获取】任务");
			traderCollectDataResult = threadPool.submit(oprs);
			log.info("尝试启动……【交易相关数据获取】任务……成功");
		}
	}

	/**
	 * 停止获取账户持仓的调度
	 */
	public static void shutdownTradingCollectJob() {
		synchronized (metux) {
			log.info("尝试关闭……【交易相关数据获取】任务");
			if(traderCollectDataResult == null || traderCollectDataResult.isCancelled() || traderCollectDataResult.isDone()){
				log.warn("【交易相关数据获取】任务……已经处于停止 或者 取消状态");
				return;
			}
			try {
				tradingKillSemaphore.acquire();
				if(traderCollectDataResult.cancel(true)){
					if(traderCollectDataResult.isCancelled()){
						log.info("【交易相关数据获取】任务……关闭成功");
						tradingKillSemaphore.release();
						return;
					}
				}
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();;
			}
			tradingKillSemaphore.release();
			log.error("【交易相关数据获取】任务……关闭失败");
		}
	}

	/**
	 * 开始获取账户行情的调度
	 */
	public static void startMarketDataCollectJob() {
		synchronized (metux) {
			if (marketDataResult != null && !marketDataResult.isCancelled() && !marketDataResult.isDone()) {
				log.warn("【行情相关数据获取】任务……已经处于启动状态");
				return;
			}
			
		}
		Runnable marketOprs = new Runnable() {
			@Override
			public void run() {
				while (!Thread.currentThread().isInterrupted()) {
					log.info("【行情数据获取】操作……启动成功");
//					CollectedDataCache.instrumentsMarketDataMap.clear();
					new MarketCollector().collect();
				}
				log.warn("有其它线程关闭了……【行情数据获取】操作……当前任务终止");
			}
		};
		log.info("尝试启动……【行情数据获取】任务");
		marketDataResult = threadPool.submit(marketOprs);
		log.info("尝试启动……【行情数据获取】任务……成功");
	}

	/**
	 * 停止获取账户行情的调度
	 */
	public static void shutdownMarketDataCollectJob() {
		log.info("尝试关闭……【行情数据获取】任务");
		if(marketDataResult == null || marketDataResult.isCancelled() || marketDataResult.isDone()){
			log.warn("【行情数据获取】任务……已经处于停止 或者 取消状态");
			return;
		}
		if(marketDataResult.cancel(true)){
			if(marketDataResult.isCancelled()){
				log.info("【行情数据获取】任务……关闭成功");
				return;
			}
		}
		log.warn("【行情数据获取】任务……关闭失败");
	}

	private static AccountMoneyCollector getAccountMoneyCollectorFromCache(
			AccountDetail accountDetail) {
		AccountMoneyCollector accountMoneyCollector = AccountMoneyCollectorCache
				.get(accountDetail);
		if (accountMoneyCollector == null) {
			AccountMoneyCollectorCache.putIfAbsent(accountDetail,
					new AccountMoneyCollector(accountDetail));
			accountMoneyCollector = AccountMoneyCollectorCache
					.get(accountDetail);
		}
		return accountMoneyCollector;
	}

	private static AccountPositionCollector getAccountPositionCollectorFromCache(
			AccountDetail accountDetail) {
		AccountPositionCollector accountPositionCollector = AccountPositionCollectorCache
				.get(accountDetail);
		if (accountPositionCollector == null) {
			AccountPositionCollectorCache.putIfAbsent(accountDetail,
					new AccountPositionCollector(accountDetail));
			accountPositionCollector = AccountPositionCollectorCache
					.get(accountDetail);
		}
		return accountPositionCollector;
	}
	
	public static void removeCollectorCache(AccountDetail accountDetail){
		AccountPositionCollectorCache.remove(accountDetail);
		AccountMoneyCollectorCache.remove(accountDetail);
	}
	

	public static void emptyCollectorCache(){
		AccountPositionCollectorCache.clear();
		AccountMoneyCollectorCache.clear();
	}
	
	/**
	 * 重置失败次数
	 */
	public static void resetFailTimesCollectorCache(){
		for(AccountPositionCollector accountPositionCollector : AccountPositionCollectorCache.values()){
			if(accountPositionCollector.getCurrentRetryTimes() < AbstractTradingCollector.RETRY_LIMIT_TIMES){
				accountPositionCollector.resetRetryTimes();
			}
		}
		for(AccountMoneyCollector accountMoneyCollector : AccountMoneyCollectorCache.values()){
			if(accountMoneyCollector.getCurrentRetryTimes() < AbstractTradingCollector.RETRY_LIMIT_TIMES){
				accountMoneyCollector.resetRetryTimes();
			}
		}
	}
	
	public static void refreshCollectedDataByAccount(String investorID){
		AccountDetail accountDetail = AssistDataCache.accountMap.get(investorID);
		if(accountDetail == null){
			return;
		}
		refreshAccountPositionByAccount(investorID);
		refreshAccountMoneyByAccount(investorID);
	}
	
	public static void refreshAccountPositionByAccount(String investorID){
		AccountDetail accountDetail = AssistDataCache.accountMap.get(investorID);
		if(accountDetail == null){
			return;
		}
		try {
			getAccountPositionCollectorFromCache(accountDetail).collect();
		} catch (CollectDataException e) {
			String message = "初始化账户【" + accountDetail.getUserId()
					+ "】的持仓信息失败，原因：" + e.getMessage();
			log.error(message);
		}
	}
	
	public static void refreshAccountMoneyByAccount(String investorID){
		AccountDetail accountDetail = AssistDataCache.accountMap.get(investorID);
		if(accountDetail == null){
			return;
		}
		try {
			getAccountMoneyCollectorFromCache(accountDetail).collect();
		} catch (CollectDataException e) {
			String message = "初始化账户【" + accountDetail.getUserId()
					+ "】的资金信息失败，原因：" + e.getMessage();
			log.error(message);
		}
	}

	public static void main(String[] args) throws InterruptedException,
			CollectDataException {
		AccountMoneyCollector accountMoneyCollector = new AccountMoneyCollector(new AccountDetail("609000001", "123456", 1));
		accountMoneyCollector.collect();
//		initBaseData();
//		startCollect();
//		Scanner input = null;
//		while (true) {
//			input = new Scanner(System.in);
//			int x = input.nextInt();
//			switch (x) {
//			case 1:
//				System.out.println(x);
//				startTradingCollectJob();
//				break;
//			case 2:
//				System.out.println(x);
//				startMarketDataCollectJob();
//				break;
//			case 3:
//				System.out.println(x);
//				shutdownTradingCollectJob();
//				break;
//			case 4:
//				System.out.println(x);
//				shutdownMarketDataCollectJob();
//				break;
//			case 5:
//				System.out.println(x);
//				startRiskControl();
//				break;
//			case 6:
//				System.out.println(x);
//				shutdownRiskControl();
//				break;
//			default:
//				break;
//			}
//			if(x == 0){
//				break;
//			}
//		}
//		if(input != null){
//			input.close();
//		}
//		 while (true) {
//		 for(String userId : CollectedDataCache.accountPositionDataMap.keySet()){
//			 System.out.println(CollectedDataCache.accountPositionDataMap.get(userId).toString());
//		 }
//		 for(String userId : CollectedDataCache.accountMoneyDataMap.keySet()){
//		 System.out.println(CollectedDataCache.accountMoneyDataMap.get(userId).toString());
//		 }
//		 for(String userId :
//		 CollectedDataCache.instrumentsMarketDataMap.keySet()){
//		 System.out.println(CollectedDataCache.instrumentsMarketDataMap.get(userId).toString());
//		 }
//		 Thread.sleep(300);
//		 }
	}
}
