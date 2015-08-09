package com.xun360.guojin.dataexchange.collector;

import net.jctp.CThostFtdcQryTradingAccountField;
import net.jctp.CThostFtdcTradingAccountField;
import net.jctp.JctpException;
import net.jctp.TraderApi;

import com.xun360.guojin.dataexchange.cfg.ExchangeConfig;
import com.xun360.guojin.dataexchange.exception.CollectDataException;
import com.xun360.guojin.dataexchange.factory.TraderApiEntryFactory;
import com.xun360.guojin.dataexchange.model.AccountDetail;
import com.xun360.guojin.dataexchange.model.WrapAccountTradingMoney;

/**
 * 用户资金情况的收集器
 * 
 * @author jw
 * 
 */
public class AccountMoneyCollector extends AbstractTradingCollector{

	public AccountMoneyCollector(AccountDetail account) {
		super(account);
	}
	
	@Override
	public void collect() throws CollectDataException {
		if(retryTimes < 0){
			log.error("无法尝试获取账户" + account.getUserId() + "的资金信息……已经失败" + RETRY_LIMIT_TIMES +"次");
			return;
		}
		TraderApi traderApi = null;
		try {
			traderApi = TraderApiEntryFactory.getFromCached(account);
			if(log.isDebugEnabled()){
				log.debug("尝试获取账户" + account.getUserId() + "的资金信息……");
			}
			// 获取资金信息
			log.debug("获取用户" + account.getUserId() + "的资金信息……");
			CThostFtdcQryTradingAccountField cThostFtdcTradingAccountField = new CThostFtdcQryTradingAccountField();
			cThostFtdcTradingAccountField.InvestorID = account.getUserId();
			cThostFtdcTradingAccountField.BrokerID = ExchangeConfig.BROKER_ID;
			CThostFtdcTradingAccountField rsCThostFtdcTradingAccountField = traderApi.SyncReqQryTradingAccount(cThostFtdcTradingAccountField);
//			CThostFtdcTradingAccountField rsCThostFtdcTradingAccountField = traderApi.SyncTradingAccountInfo(cThostFtdcTradingAccountField);
			WrapAccountTradingMoney wrapAccountTradingMoney = null;
			if(rsCThostFtdcTradingAccountField != null && (wrapAccountTradingMoney = CollectedDataCache.accountMoneyDataMap.putIfAbsent(account.getUserId(), new WrapAccountTradingMoney(rsCThostFtdcTradingAccountField))) != null){
				wrapAccountTradingMoney.updateByTradingAccountField(rsCThostFtdcTradingAccountField);
			}
		} catch (JctpException e) {
			String message = "帐号" + account.getUserId() + "获取资金信息 调用交易相关数据接口 异常：" + e.getMessage();
			if(--retryTimes >= 0){
				log.error(message + "尝试建立帐号链接，尝试第" + (3 - retryTimes) + "次……共" + RETRY_LIMIT_TIMES + "次");
//				try {
//					TraderApiEntryFactory.renewCachedAndGet(account);
//					collect();
//				} catch (JctpException e1) {
//				}
			}
			throw new CollectDataException(message);
		} catch (Exception e) {
			throw new CollectDataException(e.getMessage());
		} 
	}
	
	

//	public void collect() throws CollectDataException {
//		TraderApi traderApi = null;
//		try {
//			traderApi = TraderApiEntryFactory.getTraderApiHasLogin(account.getUserId(), account.getPassword());
//			if(log.isDebugEnabled()){
//				log.debug("尝试获取账户" + account.getUserId() + "的持仓信息……");
//			}
//			// 获取资金信息
//			log.debug("获取用户" + account.getUserId() + "的资金信息……");
//			CThostFtdcTradingAccountField cThostFtdcTradingAccountField = new CThostFtdcTradingAccountField();
//			cThostFtdcTradingAccountField.AccountID = account.getUserId();
//			cThostFtdcTradingAccountField.BrokerID = ExchangeConfig.BROKER_ID;
//			CThostFtdcTradingAccountField rsCThostFtdcTradingAccountField = traderApi.SyncTradingAccountInfo(cThostFtdcTradingAccountField);
//			WrapAccountTradingMoney wrapAccountTradingMoney = null;
//			if(rsCThostFtdcTradingAccountField != null && (wrapAccountTradingMoney = CollectedDataCache.accountMoneyDataMap.putIfAbsent(account.getUserId(), new WrapAccountTradingMoney(rsCThostFtdcTradingAccountField))) != null){
//				wrapAccountTradingMoney.updateByTradingAccountField(rsCThostFtdcTradingAccountField);
//			}
//			CThostFtdcUserLogoutField logout = new CThostFtdcUserLogoutField(
//					ExchangeConfig.BROKER_ID, account.getUserId());
//			traderApi.SyncReqUserLogout(logout);// 同步登出
//			log.debug("用户" + account.getUserId() + "登出");
//		} catch (JctpException e) {
//			String message = "帐号" + account.getUserId() + "获取交易相关数据出现异常：" + e.getMessage();
//			log.error(message);
//			throw new CollectDataException(message);
//		} catch (Exception e) {
//			throw new CollectDataException(e.getMessage());
//		} finally{
//			TraderApiEntryFactory.eliminateTraderApi(traderApi,account.getUserId());
//		}
//	}
}
