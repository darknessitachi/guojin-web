package com.xun360.guojin.configure.bean;

import java.util.List;

import com.xun360.guojin.configure.bean.indecator.Indecator;

/**
 * 帐号信息
 * 包括：clientid，investorID（期货帐号），indecator（风控指标）
 * @author root
 *
 */
public class ConfigureInfo {
	private int clientID;
	private String InvestorID;
	private List<Indecator> indecator;
	
	public void riskControl(){
		for(int i=0;i<indecator.size();i++){
			indecator.get(i).riskControl(clientID);
		}
	}
	
	public int getStat(){
		int stat=0;
		for(int i=0;i<indecator.size();i++){
			stat=Math.max(stat, indecator.get(i).getStat());
		}
		return stat;
	}
	//------------get set-------------
	public int getClientID() {
		return clientID;
	}
	public void setClientID(int clientID) {
		this.clientID = clientID;
	}
	public List<Indecator> getIndecator() {
		return indecator;
	}
	public void setIndecator(List<Indecator> indecator) {
		this.indecator = indecator;
	}
	public String getInvestorID() {
		return InvestorID;
	}
	public void setInvestorID(String investorID) {
		InvestorID = investorID;
	}
	
}
