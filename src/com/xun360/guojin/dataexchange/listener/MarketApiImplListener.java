package com.xun360.guojin.dataexchange.listener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import net.jctp.CThostFtdcDepthMarketDataField;
import net.jctp.CThostFtdcRspInfoField;
import net.jctp.CThostFtdcRspUserLoginField;
import net.jctp.CThostFtdcSpecificInstrumentField;
import net.jctp.CThostFtdcUserLogoutField;
import net.jctp.MdApiListener;

import com.xun360.guojin.configure.bean.RiskComponent;
import com.xun360.guojin.dataexchange.cfg.ExchangeConfig;
import com.xun360.guojin.dataexchange.collector.CollectedDataCache;
import com.xun360.guojin.dataexchange.model.WrapAccountPosition;
import com.xun360.guojin.dataexchange.model.WrapAccountPosition.WrapAccountPositionSnapShot;

public class MarketApiImplListener implements MdApiListener {

	private final ConcurrentMap<String, AtomicLong> statMap;
	
	private final ExecutorService executorService = Executors.newSingleThreadExecutor();
	
	private Future<?> futureTask;
	
//	private final CyclicBarrier cyclicBarrier = new CyclicBarrier(1);
//	private final AtomicBoolean atomicBoolean = new AtomicBoolean(true);
//	private final Semaphore semaphore = new Semaphore(1);
	
	public MarketApiImplListener(ConcurrentMap<String, AtomicLong> statMap) {
		super();
		this.statMap = statMap;
	}

	/**
	 * 行情回调方法，触发一系列动作
	 */
	public void OnRtnDepthMarketData(
			CThostFtdcDepthMarketDataField pDepthMarketData) {
		/**调用行情统计**/
		AtomicLong count = statMap.get(pDepthMarketData.InstrumentID);
		if (count == null) {
			statMap.put(pDepthMarketData.InstrumentID,
					count = new AtomicLong(0));
		}
		count.incrementAndGet();
		/**行情数据更新**/
		CollectedDataCache.instrumentsMarketDataMap.put(
				pDepthMarketData.InstrumentID, pDepthMarketData);
		/**更新 持仓 和 资金信息**/
		for(String investorId : CollectedDataCache.accountPositionDataMap.keySet()){
			List<WrapAccountPosition> accountPositions = CollectedDataCache.accountPositionDataMap.get(investorId);
			//触发对应持仓合约的 持仓信息的更新
			boolean updatePosi = false;
			List<WrapAccountPositionSnapShot> accountPositionSnapShots = new ArrayList<WrapAccountPosition.WrapAccountPositionSnapShot>();
			for(WrapAccountPosition accountPosition : accountPositions){
				//根据行情的对应合约号更新对应的持仓信息 
				if(pDepthMarketData.InstrumentID.equals(accountPosition.getSnapShot().InstrumentID)){
					accountPosition.updateByDepthMarketDataField(pDepthMarketData);
					updatePosi = true;
				}
				accountPositionSnapShots.add(accountPosition.getSnapShot());
			}
			//触发对应账户的资金情况的更新
			if(updatePosi){
				//如果有持仓变化 则更新资金情况
				CollectedDataCache.accountMoneyDataMap.get(investorId).updateByWrapAccountPosition(accountPositionSnapShots);
			}
		}
		//将风控执行改为定期执行2秒一次
//		/**调用风险计算 串行执行**/
//		if(ExchangeConfig.RISK_CONTROL){
//			if(futureTask == null || (futureTask != null && futureTask.isDone())){
//				Runnable run =  new Runnable() {
//					public void run() {
//						RiskComponent.computeAllInvestorRisk();
//					}
//				};
//				futureTask = executorService.submit(run);
//			}
//		}
	}

	@Override
	public void OnRspUserLogout(CThostFtdcUserLogoutField pUserLogout,
			CThostFtdcRspInfoField pRspInfo, int nRequestID, boolean bIsLast) {

	}

	@Override
	public void OnRspUserLogin(CThostFtdcRspUserLoginField pRspUserLogin,
			CThostFtdcRspInfoField pRspInfo, int nRequestID, boolean bIsLast) {
	}

	@Override
	public void OnRspUnSubMarketData(
			CThostFtdcSpecificInstrumentField pSpecificInstrument,
			CThostFtdcRspInfoField pRspInfo, int nRequestID, boolean bIsLast) {

	}

	@Override
	public void OnRspSubMarketData(
			CThostFtdcSpecificInstrumentField pSpecificInstrument,
			CThostFtdcRspInfoField pRspInfo, int nRequestID, boolean bIsLast) {
	}

	@Override
	public void OnRspError(CThostFtdcRspInfoField pRspInfo, int nRequestID,
			boolean bIsLast) {

	}

	@Override
	public void OnHeartBeatWarning(int nTimeLapse) {

	}

	@Override
	public void OnFrontDisconnected(int nReason) {
	}

	@Override
	public void OnFrontConnected() {
	}
}
