package com.xun360.guojin.dataexchange.factory;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import net.jctp.CThostFtdcQrySettlementInfoConfirmField;
import net.jctp.CThostFtdcReqAuthenticateField;
import net.jctp.CThostFtdcReqUserLoginField;
import net.jctp.CThostFtdcSettlementInfoConfirmField;
import net.jctp.CThostFtdcUserLogoutField;
import net.jctp.JctpConstants;
import net.jctp.JctpException;
import net.jctp.TraderApi;

import org.apache.log4j.Logger;

import com.xun360.guojin.dataexchange.cfg.ExchangeConfig;
import com.xun360.guojin.dataexchange.listener.TraderApiImplListener;
import com.xun360.guojin.dataexchange.model.AccountDetail;

public class TraderApiEntryFactory {
	protected static final ConcurrentMap<AccountDetail, TraderApi> traderApiCache = new ConcurrentHashMap<AccountDetail, TraderApi>();
	
	//黑名单
	private static final ConcurrentMap<AccountDetail, AtomicInteger> blackList = new ConcurrentHashMap<AccountDetail, AtomicInteger>();
	
	private static final int RETRY_TIMES = 3;

	private static final Logger log = Logger.getLogger(TraderApiEntryFactory.class);

//	public static TraderApi renewCachedAndGet(AccountDetail account)
//			throws JctpException {
//		TraderApi traderApi = traderApiCache.get(account);
//		if (traderApi != null) {
//			TraderApi tmptraderApi = getTraderApiHasLogin(account.getUserId(),
//					account.getPassword());
//			if (traderApiCache.replace(account, traderApi, tmptraderApi)) {
//				eliminateTraderApi(traderApi, account.getUserId());
//			} else {
//				eliminateTraderApi(tmptraderApi, account.getUserId());
//			}
//			return traderApiCache.get(account);
//		} else {
//			return getFromCached(account);
//		}
//	}
	
	public static void checkAccountCorrect(AccountDetail account) throws JctpException{
		try{
			//首先重置对应帐号的黑名单
			resetAccountInBlackList(account);
			getFromCached(account);
		}catch(JctpException e){
			//捕获错误，并对错误进行再次包装
			if(e.getErrorCode() == JctpException.ERROR_SYNC_REQUEST_TIMEOUT || e.getErrorCode() == JctpException.ERROR_NOT_CONNECTED){
				//超时错误
				throw new JctpException(JctpException.ERROR_SYNC_REQUEST_TIMEOUT,"由于连接交易系统超时，无法进行用户验证；请于开盘的时间段，进行相关操作！");
			}else if(e.getErrorCode() == 3){
				//用户名 或 密码错误
				throw new JctpException(3,"您填写的用户名或密码错误！请核实！");
			}
		}
	}
	
	public static TraderApi getFromCached(AccountDetail account) throws JctpException{
		return getFromCached(account,account.getAutoConfirm() == 1);
	}

	private static TraderApi getFromCached(AccountDetail account,boolean needConfirmResult)
			throws JctpException {
		blackList.putIfAbsent(account,new AtomicInteger(RETRY_TIMES));
		if(blackList.get(account).get() <= 0){//如果已经超过次数,直接抛异常
			throw new JctpException(500,"账户[" + account.getUserId() + "]接已经操作重试链接次数" + RETRY_TIMES + "次，进入黑名单;不予返回API实例,重启服务才能重新启用，可能原因：远程交易服务器关闭 或者 登录密码不对！");
		}
		TraderApi traderApi = traderApiCache.get(account);
		try{
			if (traderApi == null) {
				TraderApi tmptraderApi = getTraderApiHasLogin(account.getUserId(),
						account.getPassword(),needConfirmResult);
				if (traderApiCache.putIfAbsent(account, tmptraderApi) == null) {
					traderApi = tmptraderApi;
				} else {
					traderApi = traderApiCache.get(account);
				}
			}else{
				//检查api的状态 并做适当的修复
				checkApiStatAndModify(traderApi, account.getUserId(), account.getPassword(),needConfirmResult);
			}
		}catch(JctpException e){
			blackList.get(account).decrementAndGet();//如果已经超过次数
			throw e;
		}
		return traderApi;
	}
	
	private static void checkApiStatAndModify(TraderApi traderApi,String userId,String userPasswd,boolean needConfirmResult) throws JctpException{
		try{
			if (!traderApi.isConnected()) {
				log.info("账户[" + userId + "]通过[" + ExchangeConfig.TRADER_FRONT_URL + "]["
						+ ExchangeConfig.BROKER_ID + "]连接 交易接口");
				traderApi.SyncConnect(ExchangeConfig.TRADER_FRONT_URL);
			}
			if (!traderApi.isLogin()) {
				//如果不配置就去认证
				if(ExchangeConfig.AUTH_CODE != null){
					if (log.isDebugEnabled()) {
						log.debug("账户" + userId + "认证交易接口");
					}
					CThostFtdcReqAuthenticateField authenticateField = new CThostFtdcReqAuthenticateField();
					authenticateField.UserID = userId;
					authenticateField.BrokerID = ExchangeConfig.BROKER_ID;
					authenticateField.AuthCode = ExchangeConfig.AUTH_CODE;
					authenticateField.UserProductInfo = ExchangeConfig.USER_PRODUCT_INFO;
					traderApi.SyncAllReqAuthenticate(authenticateField);
					traderApi.SyncReqAuthenticate(authenticateField);
				}
				if (log.isDebugEnabled()) {
					log.debug("账户" + userId + "登录交易接口");
				}
				CThostFtdcReqUserLoginField login = new CThostFtdcReqUserLoginField();
				login.BrokerID = ExchangeConfig.BROKER_ID;
				login.UserID = userId;
				login.Password = userPasswd;
				traderApi.SyncReqUserLogin(login);// 同步登录
				traderApi.setLogin(true);
			}
			if(needConfirmResult && !isConfirmResult(traderApi, userId)){//如果没有确认，并且要求确认，进行确认
				confirmResult(traderApi, userId);
			}
		}catch(JctpException e){
			eliminateTraderApi(traderApi, userId);
			throw e;
		}
	}

	private static TraderApi getTraderApiHasLogin(String userId,
			String userPasswd,boolean needConfirmResult) throws JctpException {
		TraderApi traderApi = new TraderApi();
		traderApi.setListener(new TraderApiImplListener());
		traderApi.setAutoSleepReqQry(true);
		traderApi.SubscribePrivateTopic(JctpConstants.THOST_TERT_QUICK);
		traderApi.SubscribePublicTopic(JctpConstants.THOST_TERT_QUICK);
		checkApiStatAndModify(traderApi, userId, userPasswd, needConfirmResult);
		return traderApi;
	}
	
	public static void confirmResult(AccountDetail account) throws JctpException{
		TraderApi traderApi = traderApiCache.get(account);
		confirmResult(traderApi, account.getUserId());
	}
	
	private static void confirmResult(TraderApi traderApi,String userId) throws JctpException{
		if(traderApi == null){
			return;
		}
		try{
			String tradingDay = new SimpleDateFormat("yyyyMMdd").format(new Date());
			CThostFtdcSettlementInfoConfirmField confirmField = new CThostFtdcSettlementInfoConfirmField(ExchangeConfig.BROKER_ID, userId, tradingDay, null);
			confirmField = traderApi.SyncReqSettlementInfoConfirm(confirmField);
			traderApi.setConfirmDay(tradingDay);
			log.info("账户[" + userId + "]确认交易日期为[" + tradingDay + "] 结算……成功");
		}catch(JctpException e){
			log.error("账户[" + userId + "]通过确认结算……失败；原因：" + e.getMessage());
			throw e;
		}
	}
	
	
	private static boolean isConfirmResult(TraderApi traderApi,String userId){
		if(traderApi == null){
			return false;
		}else{
			if(traderApi.getConfirmDay() == null || "".equals(traderApi.getConfirmDay())){
				//如果没有确定日期，则查询结算时候确认
				CThostFtdcQrySettlementInfoConfirmField f = new CThostFtdcQrySettlementInfoConfirmField(ExchangeConfig.BROKER_ID, userId);
				try {
					String tradingDay = new SimpleDateFormat("yyyyMMdd").format(new Date());
					CThostFtdcSettlementInfoConfirmField confirmField = traderApi.SyncReqQrySettlementInfoConfirm(f);
					//如果confirmField == null 则说明今天没有确认
					if(confirmField != null && tradingDay.equals(confirmField.ConfirmDate) && confirmField.ConfirmTime != null && !"".equals(confirmField.ConfirmTime)){
						//如果查询返回的结算日期 等于 今天的日期，并且确认日期不为空（说明已经确认），则说明今天的结算单已经确认，并设置api的结算确认日期是今天
						traderApi.setConfirmDay(confirmField.ConfirmDate);
						return true;
					}
				} catch (JctpException e) {
					log.error("账户[" + userId + "]获取确认结算信息……失败；原因：" + e.getMessage());
				}
				return false;
			}else if(traderApi.getConfirmDay().equals(new SimpleDateFormat("yyyyMMdd").format(new Date()))){
				return true;
			}else{
				return false;
			}
		}
	}
	
	private static boolean isConfirmResult(AccountDetail account){
		TraderApi traderApi = traderApiCache.get(account);
		return isConfirmResult(traderApi,account.getUserId());
	}

	/**
	 * 注销此api
	 * 
	 * @param traderApi
	 */
	private static void eliminateTraderApi(TraderApi traderApi, String userId) {
		if (traderApi != null) {
			try {
				if (traderApi.isLogin()) {
					CThostFtdcUserLogoutField f = new CThostFtdcUserLogoutField(
							ExchangeConfig.BROKER_ID, userId);
					traderApi.SyncReqUserLogout(f);
					traderApi.setLogin(false);
				}
			} catch (JctpException ex) {
			} finally {
//				if (traderApi != null && traderApi.isConnected()) {
				if (traderApi != null) {
					traderApi.Close();
					log.info("关闭账户[" + userId + "]的[" + ExchangeConfig.TRADER_FRONT_URL + "]["
							+ ExchangeConfig.BROKER_ID + "]连接的交易接口");
					try {
						Thread.sleep(200);
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
					}
				}
			}
		}
	}
	
	
	public static void resetAccountInBlackList(AccountDetail account){
		blackList.put(account, new AtomicInteger(RETRY_TIMES));
	}

	public static void removeCacheAPI(AccountDetail accountDetail){
		TraderApi traderApi = traderApiCache.get(accountDetail);
		if(traderApi != null){
			eliminateTraderApi(traderApi, accountDetail.getUserId());
			traderApiCache.remove(accountDetail);
		}
	}
	
	public static void resetAllCacheAPI(){
		blackList.clear();//清除黑名单
		for(AccountDetail accountDetail : traderApiCache.keySet()){
			try {
				if(accountDetail.getAutoConfirm() == 1){
					//确认结算结果
					confirmResult(traderApiCache.get(accountDetail),accountDetail.getUserId());
				}
			} catch (JctpException e) {
				//清除缓存
				removeCacheAPI(accountDetail);
			}
		}
	}
	
	public static void main(String[] args) throws JctpException {
//		getFromCached(new AccountDetail("00000081", "123456",0));
		checkAccountCorrect(new AccountDetail("00000081", "1234567",1));
	}
}
