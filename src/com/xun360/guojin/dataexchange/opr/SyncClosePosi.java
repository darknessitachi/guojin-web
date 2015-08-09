package com.xun360.guojin.dataexchange.opr;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import net.jctp.CThostFtdcInputOrderField;
import net.jctp.JctpConstants;
import net.jctp.JctpException;
import net.jctp.TraderApi;

import org.apache.log4j.Logger;
import org.springframework.util.Assert;

import com.xun360.guojin.dataexchange.collector.AssistDataCache;
import com.xun360.guojin.dataexchange.factory.TraderApiEntryFactory;
import com.xun360.guojin.dataexchange.listener.TraderApiImplListener;
import com.xun360.guojin.dataexchange.model.AccountDetail;
import com.xun360.guojin.dataexchange.model.ClosePosiStat;

/**
 * 同步平仓
 * @author root
 *
 */
public class SyncClosePosi {

	private static final Logger log = Logger.getLogger(SyncClosePosi.class);
	
	//需要完成的请求 作为一种闭锁
//	private final CountDownLatch needCompleteCloseReqNum;
	
	private final List<CThostFtdcInputOrderField> cThostFtdcInputOrderFields;
	
	private ConcurrentMap<String,ClosePosiStat> closePosiStats = new ConcurrentHashMap<String, ClosePosiStat>();
	
	public SyncClosePosi(List<CThostFtdcInputOrderField> insertList) {
		Assert.notNull(insertList);
		Assert.isTrue(insertList.size() > 0);
		this.cThostFtdcInputOrderFields = insertList;
//		for(CThostFtdcInputOrderField cThostFtdcInputOrderField : insertList){
//			closeCallQueueList.add(new FutureTask<ClosePosiStat>(new CallClosePosi(new ClosePosiReq(cThostFtdcInputOrderField, uuid))));
//			closePosiStats.put(uuid,new ClosePosiStat(cThostFtdcInputOrderField.InvestorID, cThostFtdcInputOrderField.InstrumentID, 
//					(cThostFtdcInputOrderField.Direction == JctpConstants.THOST_FTDC_PD_Long ? JctpConstants.THOST_FTDC_PD_Short : JctpConstants.THOST_FTDC_PD_Long), 0, 0, cThostFtdcInputOrderField.VolumeTotalOriginal, 0));
//		}
		//闭锁 用于监控所有 要平仓的合约的进度
//		needCompleteCloseReqNum = new CountDownLatch(insertList.size());
	}
	
	/**
	 * 同步平仓，全部成交后，才返回
	 * 60秒 超时
	 * @return
	 * @throws JctpException
	 */
	public List<ClosePosiStat> syncClose() {
		CThostFtdcInputOrderField cThostFtdcInputOrderField = cThostFtdcInputOrderFields.iterator().next();
		AccountDetail account = AssistDataCache.accountMap.get(cThostFtdcInputOrderField.InvestorID);
		TraderApi traderApi = null;
		TraderApiImplListener apiImplListener = null;
		try{
			traderApi =TraderApiEntryFactory.getFromCached(account);
			apiImplListener = (TraderApiImplListener) traderApi.getListener();
			apiImplListener.syncReset(cThostFtdcInputOrderFields);
			for(CThostFtdcInputOrderField order : cThostFtdcInputOrderFields){
				log.info("平仓操作……对账户[" + order.InvestorID + "]的合约号为[" + order.InstrumentID + "]的["
						+ (order.Direction == JctpConstants.THOST_FTDC_PD_Long ? "买" : "卖") + "]合约[" + (order.CombOffsetFlag == JctpConstants.STRING_THOST_FTDC_OF_CloseToday? "平今":"平仓") + "][" + order.VolumeTotalOriginal + "]手,提交报单请求");
				traderApi.ReqOrderInsert(order);
			}
			//阻塞……用于同步成交 60秒超时
			closePosiStats = apiImplListener.getTradeResult(60, TimeUnit.SECONDS);
		} catch(JctpException e){
			log.info("平仓操作……对账户[" + cThostFtdcInputOrderField.InvestorID + "]的[平仓],出现异常" + e.getMessage());
		} catch (InterruptedException e) {
			if(apiImplListener != null){
				closePosiStats = apiImplListener.closePosiStats;
			}
		}
		return new ArrayList<ClosePosiStat>(closePosiStats.values());
	}
	

}
