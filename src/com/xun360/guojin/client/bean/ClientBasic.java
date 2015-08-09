package com.xun360.guojin.client.bean;

public class ClientBasic {

	private String investorID;
	private String password;
	private String name;
	private double initFunds;
	private double afterFunds;

	public String getInvestorID() {
		return investorID;
	}
	public void setInvestorID(String investorID) {
		this.investorID = investorID;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public double getInitFunds() {
		return initFunds;
	}
	public void setInitFunds(double initFunds) {
		this.initFunds = initFunds;
	}
	public double getAfterFunds() {
		return afterFunds;
	}
	public void setAfterFunds(double afterFunds) {
		this.afterFunds = afterFunds;
	}
	
}
