package com.xun360.guojin.configure.bean.indecator;

import com.xun360.guojin.util.bean.ClientFundsData;



/* type:
 * 1:资金全天候监控
 * 2:资金量分时段监控
 * 3:时间段开仓限制监控
 * 4:品种资金量监控
 * 5:资金回撤监控
 * 6:劣后资金量监控
 */
abstract public class Indecator {
	
	protected int id;
	protected int type;
	protected String str;//描述

	public abstract void riskControl(int clientID);
	public abstract void setValue(double now,double basic);
	public abstract int getStat();
	//------------get set-------------
	public void updateByClientFundsData(ClientFundsData clientFundsData){
		//TODO in subclass
	}
	
	public void setHasPosi(boolean has){
		//TODO in subclass when need
	}
	
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	
}
