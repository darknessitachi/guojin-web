package com.xun360.guojin.configure.bean.ceil;

import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import com.xun360.guojin.configure.bean.ClosePosiJob;
import com.xun360.guojin.util.Common;
import com.xun360.guojin.util.ErrorLog;
import com.xun360.guojin.util.TimeSolUtils;

public class LimitCeil extends Ceil{


	//从0点开始的毫秒数
	private int start=0;
	private int end=0;
	private boolean hasPosi = false;
			

	public String toString(){
		StringBuilder builder = new StringBuilder();
		builder.append("限制时间：开始[").append(TimeSolUtils.getTimeStrByMilOneDay(start))
			.append("]结束[").append(TimeSolUtils.getTimeStrByMilOneDay(end)).append("]");
		return builder.toString();
	}
	
	@Override
	protected Boolean calc() {
		Date date=new Date();
		int time=(int)((date.getTime()+TimeSolUtils.offsetHous*3600*1000)%(3600*24*1000));
		Boolean doAction = false;
		if(hasPosi && type!=0 && 
				((start < time && time < end) || (start < time+1000*3600*24 && time+1000*3600*24 < end))){//时间在区间内，非正常状态
			if(this.stat != type && type == 1){
				//上次记录是平仓，现在是警告
				doAction = true;
				this.stat = type;
			}else if(type == 2){
				//上次记录是平仓或警告，现在记录还是平仓（其实本质就是平仓了，还要记录平；警告了，就不继续警告；）
				doAction = true;
				this.stat = type;
			}
		}else{
			this.stat = 0;
		}
		return doAction;
	}
	
	@Override
	public void riskCompute(int clientID, String indicatorMsg,
			List<String> instruments) {
		if (calc()) {
			String investorID = Common.getInvestorIDByClientID(clientID);
			if(getType() == 1){//警告
				String msg = "帐号[" + investorID + "]的"
						+ indicatorMsg + ",由于：【" + toString() + "】导致警告";
				ErrorLog.message(clientID, 1, msg, msg);
				log.info(msg);
			}else{//平仓
				ClosePosiJob closePosiJob = new ClosePosiJob(clientID, indicatorMsg, instruments, 1, 100, this.toString());
				new Thread(closePosiJob) .start();
//				String msg = "帐号[" + investorID + "]的"
//						+ indicatorMsg + ",由于：【" + toString() + "】导致平仓";
//				//判断仓位
//				List<WrapAccountPosition> accountPositions = CollectedDataCache.accountPositionDataMap.get(investorID);
//				int closePostion = 0;
//				for(WrapAccountPosition accountPosition : accountPositions){
//					closePostion = accountPosition.getSnapShot().ClosePostion + closePostion;
//				}
//				if(closePostion <= 0){
//					return;
//				}
//				//平仓
//				synchronized (Common.closeStat) {
//					Boolean nowStat = Common.closeStat.get(investorID);
//					if (nowStat != null && nowStat) {
//						log.warn(msg + ";没有执行平仓，此账户已经正在平仓");
//						return;
//					}
//					Common.closeStat.put(investorID, true);
//				}
//				//开始平仓
//				try {
//					if(instruments != null && !instruments.isEmpty()){
//						ClosePositionOpr.fullClosePosi(investorID,instruments);
//					}else{
//						ClosePositionOpr.fullClosePosi(investorID);
//					}
//					ErrorLog.message(clientID, 2, msg, msg);
//					log.info(msg);
//				} catch (ClosePosiException e) {
//					log.error(msg + ",平仓失败；原因：" + e.getMessage() );
//				} finally{
//					synchronized (Common.closeStat) {
//						Common.closeStat.put(investorID, false);
//					}
//				}
			}
		}
	}
	
	//构造方法
	public LimitCeil(int type,int start,int end,boolean hasPosi){
		this.type=type;
		this.start=start;
		this.end=end;
		this.hasPosi = hasPosi;
	}
	//------------get set-------------
	public int getStart() {
		return start;
	}

	public void setStart(int start) {
		this.start = start;
	}

	public int getEnd() {
		return end;
	}

	public void setEnd(int end) {
		this.end = end;
	}

	public boolean isHasPosi() {
		return hasPosi;
	}

	public void setHasPosi(boolean hasPosi) {
		this.hasPosi = hasPosi;
	}
	
	
	public static void main(String[] args) {
		System.out.println(TimeSolUtils.offsetHous);
		Date date = new Date();
		System.out.println(TimeZone.getDefault().getOffset(date.getTime())/(3600*1000));
		System.out.println(date.getTime());
		System.out.println(System.currentTimeMillis());
		long ms = (date.getTime())%(3600*24*1000);
		System.out.println(ms/(3600*1000));
	}
	
}
