package com.xun360.guojin.configure.bean;

import java.util.List;

import org.apache.log4j.Logger;

import com.xun360.guojin.dataexchange.collector.CollectedDataCache;
import com.xun360.guojin.dataexchange.exception.ClosePosiException;
import com.xun360.guojin.dataexchange.model.ClosePosiStat;
import com.xun360.guojin.dataexchange.model.WrapAccountPosition;
import com.xun360.guojin.dataexchange.opr.ClosePositionOpr;
import com.xun360.guojin.util.Common;
import com.xun360.guojin.util.ErrorLog;

/**
 * 平仓任务
 */
public class ClosePosiJob implements Runnable{
	private final static Logger log = Logger.getLogger(ClosePosiJob.class);
	
	
	private int clientID;
	private String indicatorMsg;
	private List<String> instruments;
	private int clseWay = 0;//平仓方式 0:不平,1:全平,2:百分比
	private double closePercent = 0;// 百分比比值
	private String reason;
	
	public ClosePosiJob(int clientID, String indicatorMsg,
			List<String> instruments, int way, double percent,String reason) {
		this.clientID = clientID;
		this.indicatorMsg = indicatorMsg;
		this.instruments = instruments;
		this.clseWay = way;
		this.closePercent = percent;
		this.reason = reason;
	}

	@Override
	public void run() {
		String investorID = Common.getInvestorIDByClientID(clientID);
		String msg = "帐号[" + investorID + "]的"
				+ indicatorMsg + ",由于：【" + reason + "】导致平仓";
		//开始平仓
		try {
			//判断仓位
			List<WrapAccountPosition> accountPositions = CollectedDataCache.accountPositionDataMap.get(investorID);
			if(accountPositions == null){
				return;
			}
			int closePostion = 0;
			for(WrapAccountPosition accountPosition : accountPositions){
				closePostion = accountPosition.getSnapShot().ClosePostion + closePostion;
			}
			if(closePostion <= 0){
				return;
			}
			//执行平仓
			synchronized (Common.closeStat) {
				Boolean nowStat = Common.closeStat.get(investorID);
				if (nowStat != null && nowStat) {
					log.warn(msg + "；没有执行平仓，此账户已经正在平仓");
					return;
				}
				Common.closeStat.put(investorID, true);
			}
			
			List<ClosePosiStat> closeStat = null;
			if(clseWay == 1){
				if(instruments != null && !instruments.isEmpty()){
					closeStat = ClosePositionOpr.fullClosePosi(investorID,instruments);
				}else{
					closeStat = ClosePositionOpr.fullClosePosi(investorID);
				}
			}else if (clseWay == 2){
				if(instruments != null && !instruments.isEmpty()){
					closeStat = ClosePositionOpr.percentClosePosi(closePercent/100, investorID, instruments);
				}else{
					closeStat = ClosePositionOpr.percentClosePosi(closePercent/100, investorID);
				}
			}
			ErrorLog.message(clientID, 2, msg, msg + closeStat.toString());
			log.info(msg);
		} catch (ClosePosiException e) {
			log.error(msg + ",平仓失败；原因：" + e.getMessage() );
		} finally{
			synchronized (Common.closeStat) {
				Common.closeStat.put(investorID, false);
			}
		}
		
	}
}
