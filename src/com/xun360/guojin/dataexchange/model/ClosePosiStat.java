package com.xun360.guojin.dataexchange.model;

import net.jctp.JctpConstants;

/**平仓统计对象**/
public class ClosePosiStat{
	/**投资者帐号**/
	public final String InvestorID;
	/**合约号**/
	public final String InstrumentID;
	/**买卖（多空）**/
	public final char Direction;
	/**平仓成交均价**/
	public final double avgPrice;
	/**平仓成交总额**/
	public final double totalMoney;
	/**需平仓量**/
	public final int needCloseVolume;
	/**已平仓量**/
	public final int hasClosedVolume;
	/**错误描述**/
	public String errorDes = "";
	
	public ClosePosiStat(String investorID, String instrumentID,
			char direction, double avgPrice, double totalMoney,
			int needCloseVolume, int hasClosedVolume) {
		super();
		InvestorID = investorID;
		InstrumentID = instrumentID;
		Direction = direction;
		this.avgPrice = avgPrice;
		this.totalMoney = totalMoney;
		this.needCloseVolume = needCloseVolume;
		this.hasClosedVolume = hasClosedVolume;
	}
	
	public String toString(){
		StringBuilder builder = new StringBuilder();
		builder.append("账户:")
				.append(InvestorID)
				.append("---------成交统计：合约代码:")
				.append(InstrumentID)
				.append("|")
				.append("买卖:")
				.append(Direction == JctpConstants.THOST_FTDC_PD_Long ? "买" : "卖")
				.append("|").append("平仓成交均价:").append(avgPrice).append("|")
				.append("平仓成交总额:").append(totalMoney).append("|")
				.append("需平仓量:").append(needCloseVolume).append("|")
				.append("已平仓量:").append(hasClosedVolume).append("|");
		if(!errorDes.equals("")){
			builder.append("错误：").append(errorDes).append("|");
		}
		builder.append("\n");
		return builder.toString();
	}
	
}
