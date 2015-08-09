package com.xun360.guojin.configure.bean.indecator;

import java.util.Date;

import com.xun360.guojin.configure.bean.ceil.CloseCeil;
import com.xun360.guojin.configure.bean.ceil.WarningCeil;
import com.xun360.guojin.util.ErrorLog;
import com.xun360.guojin.util.bean.ClientFundsData;

/**
 * 时间段开仓限制
 * 
 * @author Administrator
 * 
 */
public class SpecialTime extends Indecator {

	private WarningCeil warningCeil;
	private CloseCeil closeCeil;
	//从0点开始的毫秒数
	private int start=0;
	private int end=0;
		
	protected String str;
	
	@Override
	public void riskControl(int clientID) {
		Date date=new Date();
		int time=(int)((date.getTime()+8*3600*1000)%(3600*24*1000));
		if(((start < time && time < end) || (start < time+1000*3600*24 && time+1000*3600*24 < end))){
			warningCeil.riskCompute(clientID, str, null);
			closeCeil.riskCompute(clientID, str, null);
		}
	}
	
	@Override
	public void updateByClientFundsData(ClientFundsData clientFundsData) {
		warningCeil.setBasic(clientFundsData.initFunds);
		closeCeil.setBasic(clientFundsData.initFunds);
	}

	@Override
	public void setValue(double now, double basic) {
		if(basic!=0){
			ErrorLog.log("设置了多余的参数","SpecialTime","now:"+now+",basic:"+basic);
			return ;
		}
		warningCeil.setNow(now);
		closeCeil.setNow(now);
	}
	
	@Override
	public int getStat() {
		return Math.max(warningCeil.getStat(), closeCeil.getStat());
	}
	// 构造方法
	public SpecialTime(int id, int type, String criteria, double initFunds) {
		this.id = id;
		this.type = type;
		String str[] = criteria.split("\\*");
		this.warningCeil = new WarningCeil(Double.parseDouble(str[1]),
				Integer.parseInt(str[0]), initFunds,-1);
		this.closeCeil = new CloseCeil(Double.parseDouble(str[3]),
				Integer.parseInt(str[2]), Integer.parseInt(str[4]),
				Double.parseDouble(str[5]), initFunds,-1);
		this.start=Integer.parseInt(str[6]);
		this.end=Integer.parseInt(str[7]);
		this.str="自定义时间段权益监控，";
	}

	// ------------get set-------------
	
	public int getStart() {
		return start;
	}

	public WarningCeil getWarningCeil() {
		return warningCeil;
	}

	public void setWarningCeil(WarningCeil warningCeil) {
		this.warningCeil = warningCeil;
	}

	public CloseCeil getCloseCeil() {
		return closeCeil;
	}

	public void setCloseCeil(CloseCeil closeCeil) {
		this.closeCeil = closeCeil;
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

}
