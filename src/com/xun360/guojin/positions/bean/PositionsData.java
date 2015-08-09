package com.xun360.guojin.positions.bean;

public class PositionsData {
	private String contractID;
	private String contractName;
	private char contractType;
	private int totalPositions;
	private int nowPositions;
	private int lastPositions;
	private int usablePositions;
	private double avePrice;
	private double profit;
	private double deposit;
	
	public String getContractName() {
		return contractName;
	}
	public void setContractName(String contractName) {
		this.contractName = contractName;
	}
	
	
	public char getContractType() {
		return contractType;
	}
	public void setContractType(char contractType) {
		this.contractType = contractType;
	}
	public int getTotalPositions() {
		return totalPositions;
	}
	public void setTotalPositions(int totalPositions) {
		this.totalPositions = totalPositions;
	}
	public int getNowPositions() {
		return nowPositions;
	}
	public void setNowPositions(int nowPositions) {
		this.nowPositions = nowPositions;
	}
	public int getLastPositions() {
		return lastPositions;
	}
	public void setLastPositions(int lastPositions) {
		this.lastPositions = lastPositions;
	}
	public int getUsablePositions() {
		return usablePositions;
	}
	public void setUsablePositions(int usablePositions) {
		this.usablePositions = usablePositions;
	}
	public String getContractID() {
		return contractID;
	}
	public void setContractID(String contractID) {
		this.contractID = contractID;
	}
	public double getAvePrice() {
		return avePrice;
	}
	public void setAvePrice(double avePrice) {
		this.avePrice = avePrice;
	}
	public double getProfit() {
		return profit;
	}
	public void setProfit(double profit) {
		this.profit = profit;
	}
	public double getDeposit() {
		return deposit;
	}
	public void setDeposit(double deposit) {
		this.deposit = deposit;
	}

	
}
