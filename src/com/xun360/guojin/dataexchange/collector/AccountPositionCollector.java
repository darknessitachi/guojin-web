package com.xun360.guojin.dataexchange.collector;

import java.util.ArrayList;
import java.util.List;

import net.jctp.CThostFtdcInvestorPositionField;
import net.jctp.CThostFtdcQryInvestorPositionField;
import net.jctp.JctpConstants;
import net.jctp.JctpException;
import net.jctp.TraderApi;

import com.xun360.guojin.dataexchange.cfg.ExchangeConfig;
import com.xun360.guojin.dataexchange.exception.CollectDataException;
import com.xun360.guojin.dataexchange.factory.TraderApiEntryFactory;
import com.xun360.guojin.dataexchange.model.AccountDetail;
import com.xun360.guojin.dataexchange.model.InstrumentDetialForInvestor;
import com.xun360.guojin.dataexchange.model.WrapAccountPosition;

/**
 * 用户持仓情况的收集器
 * 
 * @author jw
 * 
 */
public class AccountPositionCollector extends AbstractTradingCollector {


	public AccountPositionCollector(AccountDetail account) {
		super(account);
	}
	
	public void collect() throws CollectDataException {
		if(retryTimes < 0){
			log.error("无法尝试获取账户" + account.getUserId() + "的持仓信息……已经失败" + RETRY_LIMIT_TIMES +"次");
			return;
		}
		TraderApi traderApi = null;
		try {
			traderApi = TraderApiEntryFactory.getFromCached(account);
			if(log.isDebugEnabled()){
				log.debug("尝试获取账户" + account.getUserId() + "的持仓信息……");
			}
			// 获取持仓
			log.debug("获取用户" + account.getUserId() + "的持仓……");
			CThostFtdcQryInvestorPositionField cThostFtdcQryInvestorPositionField = new CThostFtdcQryInvestorPositionField(
					ExchangeConfig.BROKER_ID, account.getUserId(), null);
			CThostFtdcInvestorPositionField[] cThostFtdcInvestorPositionFields = traderApi
					.SyncAllReqQryInvestorPosition(cThostFtdcQryInvestorPositionField);
			List<WrapAccountPosition> wrapAccountPositions = new ArrayList<WrapAccountPosition>(); 
			for(CThostFtdcInvestorPositionField cThostFtdcInvestorPositionField : cThostFtdcInvestorPositionFields){
				if(cThostFtdcInvestorPositionField.Position > 0){
					wrapAccountPositions.add(new WrapAccountPosition(cThostFtdcInvestorPositionField));
					//求出保证金率 ，并填充到instrumentsMap--> InstrumentDetialForInvestor 【LongMarginRatio】【ShortMarginRatio】
					InstrumentDetialForInvestor detialForInvestor = CollectedDataCache.instrumentsMap.get(cThostFtdcInvestorPositionField.InstrumentID);
					//合约占用保证金 =（【持仓最新价】*【可平量】*【合约乘数】*【保证金率】）
					//保证金率 = 合约占用保证金/（【持仓最新价】*【可平量】*【合约乘数】）
					if(cThostFtdcInvestorPositionField.PosiDirection == JctpConstants.THOST_FTDC_PD_Long){
						detialForInvestor.LongMarginRatio.put(account.getUserId(), cThostFtdcInvestorPositionField.UseMargin / (cThostFtdcInvestorPositionField.SettlementPrice*(cThostFtdcInvestorPositionField.TodayPosition + cThostFtdcInvestorPositionField.YdPosition)*detialForInvestor.VolumeMultiple));
					}else{
						detialForInvestor.ShortMarginRatio.put(account.getUserId(), cThostFtdcInvestorPositionField.UseMargin / (cThostFtdcInvestorPositionField.SettlementPrice*(cThostFtdcInvestorPositionField.TodayPosition + cThostFtdcInvestorPositionField.YdPosition)*detialForInvestor.VolumeMultiple));
					}
				}
			}
			CollectedDataCache.accountPositionDataMap.put(account.getUserId(),wrapAccountPositions);
		} catch (JctpException e) {
			String message = "帐号" + account.getUserId() + "获取交易相关数据出现异常：" + e.getMessage();
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
//			// 获取持仓
//			log.debug("获取用户" + account.getUserId() + "的持仓……");
//			CThostFtdcQryInvestorPositionField cThostFtdcQryInvestorPositionField = new CThostFtdcQryInvestorPositionField(
//					ExchangeConfig.BROKER_ID, account.getUserId(), null);
//			CThostFtdcInvestorPositionField[] cThostFtdcInvestorPositionFields = traderApi
//					.SyncAllReqQryInvestorPosition(cThostFtdcQryInvestorPositionField);
//			List<WrapAccountPosition> wrapAccountPositions = new ArrayList<WrapAccountPosition>(); 
//			for(CThostFtdcInvestorPositionField cThostFtdcInvestorPositionField : cThostFtdcInvestorPositionFields){
//				wrapAccountPositions.add(new WrapAccountPosition(cThostFtdcInvestorPositionField));
//			}
//			CollectedDataCache.accountPositionDataMap.put(account.getUserId(),wrapAccountPositions);
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
