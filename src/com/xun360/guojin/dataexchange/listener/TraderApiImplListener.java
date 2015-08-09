package com.xun360.guojin.dataexchange.listener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import net.jctp.CThostFtdcInputOrderField;
import net.jctp.CThostFtdcRspInfoField;
import net.jctp.CThostFtdcRspUserLoginField;
import net.jctp.CThostFtdcSettlementInfoConfirmField;
import net.jctp.CThostFtdcSettlementInfoField;
import net.jctp.CThostFtdcTradeField;
import net.jctp.CThostFtdcUserLogoutField;
import net.jctp.JctpConstants;
import net.jctp.TraderApiAdapter;

import com.xun360.guojin.dataexchange.collector.CollectedDataCache;
import com.xun360.guojin.dataexchange.model.ClosePosiStat;

public class TraderApiImplListener extends TraderApiAdapter {
	
	//闭锁 进行阻塞控制
	private CountDownLatch countDownLatch;
	//orderRef递增
	private final AtomicInteger orderRef = new AtomicInteger(0);
	//成交的报单详情
	private final ConcurrentHashMap<String, CopyOnWriteArrayList<CThostFtdcTradeField>> responseQueueMap = new ConcurrentHashMap<String, CopyOnWriteArrayList<CThostFtdcTradeField>>();
	//提交的报单
	private final ConcurrentHashMap<String, CThostFtdcInputOrderField> submittedOrders = new ConcurrentHashMap<String, CThostFtdcInputOrderField>();
	//每份报单需要交易完成的量
	private final ConcurrentHashMap<String, AtomicInteger> needCompleteVolume = new ConcurrentHashMap<String, AtomicInteger>();
	//报单成交统计
	public ConcurrentMap<String,ClosePosiStat> closePosiStats = new ConcurrentHashMap<String, ClosePosiStat>();

	public void syncReset(List<CThostFtdcInputOrderField> orders){
		//重设submittedOrders、needCompleteVolume、closePosiStats
		closePosiStats.clear();
		submittedOrders.clear();
		needCompleteVolume.clear();
		for(CThostFtdcInputOrderField cThostFtdcInputOrderField : orders){
			String order_ref = String.valueOf(orderRef.getAndIncrement());
			cThostFtdcInputOrderField.OrderRef = order_ref;
			submittedOrders.put(order_ref, cThostFtdcInputOrderField);
			needCompleteVolume.put(order_ref, new AtomicInteger(cThostFtdcInputOrderField.VolumeTotalOriginal));
			closePosiStats.put(order_ref,new ClosePosiStat(cThostFtdcInputOrderField.InvestorID, cThostFtdcInputOrderField.InstrumentID, 
					(cThostFtdcInputOrderField.Direction == JctpConstants.THOST_FTDC_PD_Long ? JctpConstants.THOST_FTDC_PD_Short : JctpConstants.THOST_FTDC_PD_Long), 0, 0, cThostFtdcInputOrderField.VolumeTotalOriginal, 0));
		}
		//重设responseQueueMap
		responseQueueMap.clear();
		//重设countDownLatch
		countDownLatch = new CountDownLatch(orders.size());
	}
	
	public ConcurrentMap<String,ClosePosiStat> getTradeResult(long timeout, TimeUnit unit) throws InterruptedException{
		countDownLatch.await(timeout, unit);
		return closePosiStats;
	}
	
	public void OnRtnTrade(CThostFtdcTradeField pTrade) {
		if(responseQueueMap.get(pTrade.OrderRef) == null){
			CopyOnWriteArrayList<CThostFtdcTradeField> trades = new CopyOnWriteArrayList<CThostFtdcTradeField>();
			trades.add(pTrade);
			if(responseQueueMap.putIfAbsent(pTrade.OrderRef, trades) != null){
				responseQueueMap.get(pTrade.OrderRef).add(pTrade);
			}
		}else{
			responseQueueMap.get(pTrade.OrderRef).add(pTrade);
		}
		// 多头保证金率
		double LongMarginRatio = CollectedDataCache.instrumentsMap
				.get(pTrade.InstrumentID).LongMarginRatio.get(pTrade.InvestorID);
		LongMarginRatio = (LongMarginRatio > 1 ? 0 : LongMarginRatio);
		// 空头保证金率
		double ShortMarginRatio = CollectedDataCache.instrumentsMap
				.get(pTrade.InstrumentID).ShortMarginRatio.get(pTrade.InvestorID);
		ShortMarginRatio = (ShortMarginRatio > 1 ? 0 : ShortMarginRatio);
		// 合约乘数
		int VolumeMultiple = CollectedDataCache.instrumentsMap
				.get(pTrade.InstrumentID).VolumeMultiple;
		/**需平仓量**/
		int needCloseVolume = submittedOrders.get(pTrade.OrderRef).VolumeTotalOriginal;
		/**平仓成交总额**/
		double totalMoney = 0;
		/**已平仓量**/
		int hasClosedVolume = 0;
		for(CThostFtdcTradeField cThostFtdcTradeField : responseQueueMap.get(pTrade.OrderRef)){
			totalMoney = totalMoney + cThostFtdcTradeField.Price * cThostFtdcTradeField.Volume * (pTrade.Direction
					== JctpConstants.THOST_FTDC_PD_Long ?  LongMarginRatio : ShortMarginRatio) * VolumeMultiple;
			hasClosedVolume = hasClosedVolume + cThostFtdcTradeField.Volume;
		}
		/**平仓成交均价**/
		double avgPrice = hasClosedVolume != 0 ? totalMoney/hasClosedVolume/VolumeMultiple/((pTrade.Direction
				== JctpConstants.THOST_FTDC_PD_Long ?  LongMarginRatio : ShortMarginRatio)) : 0;
		ClosePosiStat closePosiStat = new ClosePosiStat(pTrade.InvestorID, pTrade.InstrumentID, pTrade.Direction, avgPrice, totalMoney, needCloseVolume, hasClosedVolume);
		closePosiStats.put(pTrade.OrderRef, closePosiStat);
		if(needCompleteVolume.get(pTrade.OrderRef).addAndGet(-pTrade.Volume) <= 0){
			countDownLatch.countDown();
		}
	}

	public void OnErrRtnOrderInsert(
			CThostFtdcInputOrderField pInputOrder,
			CThostFtdcRspInfoField pRspInfo) {
		super.OnErrRtnOrderInsert(pInputOrder, pRspInfo);
		/**错误原因**/
		closePosiStats.get(pInputOrder.OrderRef).errorDes = pRspInfo.ErrorMsg;
		countDownLatch.countDown();
	}

	@Override
	public void OnRspQrySettlementInfoConfirm(
			CThostFtdcSettlementInfoConfirmField pSettlementInfoConfirm,
			CThostFtdcRspInfoField pRspInfo, int nRequestID, boolean bIsLast) {
		// TODO Auto-generated method stub
		super.OnRspQrySettlementInfoConfirm(pSettlementInfoConfirm, pRspInfo,
				nRequestID, bIsLast);
	}

	@Override
	public void OnRspSettlementInfoConfirm(
			CThostFtdcSettlementInfoConfirmField pSettlementInfoConfirm,
			CThostFtdcRspInfoField pRspInfo, int nRequestID, boolean bIsLast) {
		// TODO Auto-generated method stub
		super.OnRspSettlementInfoConfirm(pSettlementInfoConfirm, pRspInfo, nRequestID,
				bIsLast);
	}

	@Override
	public void OnRspQrySettlementInfo(
			CThostFtdcSettlementInfoField pSettlementInfo,
			CThostFtdcRspInfoField pRspInfo, int nRequestID, boolean bIsLast) {
		// TODO Auto-generated method stub
		super.OnRspQrySettlementInfo(pSettlementInfo, pRspInfo, nRequestID, bIsLast);
	}
	
	
}

