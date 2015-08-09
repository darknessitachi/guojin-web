package com.xun360.guojin.configure.bean.indecator;

import com.xun360.guojin.configure.bean.ceil.CloseCeil;
import com.xun360.guojin.configure.bean.ceil.WarningCeil;

/**
 * 回撤资金监控
 * @author Administrator
 *
 */
public class Reback extends Indecator{

	private WarningCeil warningCeil;
	private CloseCeil closeCeil;
	
	protected String str;
	
	@Override
	public void riskControl(int clientID) {
		warningCeil.riskCompute(clientID, str, null);
		closeCeil.riskCompute(clientID, str, null);
	}
	
	@Override
	public void setValue(double now, double basic) {
		warningCeil.setNow(now);
		closeCeil.setNow(now);
		warningCeil.setBasic(basic);
		closeCeil.setBasic(basic);
	}
	
	@Override
	public int getStat() {
		return Math.max(warningCeil.getStat(), closeCeil.getStat());
	}
	//构造方法
	public Reback(int id,int type,String criteria){
		this.id=id;
		this.type=type;
		String str[]=criteria.split("\\*");
		this.warningCeil=new WarningCeil(Double.parseDouble(str[1]), 
				Integer.parseInt(str[0]), 
				0,1);//高于设置警告
		this.closeCeil=new CloseCeil(Double.parseDouble(str[3]), 
				Integer.parseInt(str[2]), 
				Integer.parseInt(str[4]), 
				Double.parseDouble(str[5]), 
				0,1);//高于设置平仓
		this.str="日内资金回撤监控,";
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
