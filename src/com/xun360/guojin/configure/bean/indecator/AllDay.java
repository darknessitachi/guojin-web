package com.xun360.guojin.configure.bean.indecator;

import com.xun360.guojin.configure.bean.ceil.CloseCeil;
import com.xun360.guojin.configure.bean.ceil.WarningCeil;
import com.xun360.guojin.util.ErrorLog;
import com.xun360.guojin.util.bean.ClientFundsData;

/**
 * 资金全天候监控
 * @author Administrator
 *
 */
public class AllDay extends Indecator{

	private WarningCeil warningCeil;
	private CloseCeil closeCeil;
	
	protected String str;
	
	@Override
	public void riskControl(int clientID) {
		warningCeil.riskCompute(clientID, str, null);
		closeCeil.riskCompute(clientID, str, null);
	}
	
	@Override
	public void updateByClientFundsData(ClientFundsData clientFundsData) {
		warningCeil.setBasic(clientFundsData.initFunds);
		closeCeil.setBasic(clientFundsData.initFunds);
	}



	@Override
	public void setValue(double now, double basic) {
		if(basic!=0){
			ErrorLog.log("设置了多余的参数","Allay","now:"+now+",basic:"+basic);
			return ;
		}
		warningCeil.setNow(now);
		closeCeil.setNow(now);
	}
	
	@Override
	public int getStat() {
		return Math.max(warningCeil.getStat(), closeCeil.getStat());
	}
	//构造方法
	public AllDay(int id,int type,String criteria,double initFunds){
		this.id=id;
		this.type=type;
		String str[]=criteria.split("\\*");
		this.warningCeil=new WarningCeil(Double.parseDouble(str[1]), 
				Integer.parseInt(str[0]), 
				initFunds,-1);//低于设置报警
		this.closeCeil=new CloseCeil(Double.parseDouble(str[3]), 
				Integer.parseInt(str[2]), 
				Integer.parseInt(str[4]), 
				Double.parseDouble(str[5]), 
				initFunds,-1);//低于设置平仓
		this.str="权益监控，";
	}
	
	//------------get set-------------
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
	
	
}
