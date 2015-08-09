package com.xun360.guojin.dataexchange.model;

import com.xun360.guojin.dataexchange.collector.CollectedDataCache;

import net.jctp.CThostFtdcDepthMarketDataField;
import net.jctp.CThostFtdcInvestorPositionField;
import net.jctp.JctpConstants;

/**
 * 持仓信息包装对象
 * 
 * @author root
 *
 */
public class WrapAccountPosition {

	private WrapAccountPositionSnapShot entry;

	/**
	 * 内部持有类，将状态一致性保存到对象内部； 外部多线程的更改将只会更新entry的引用； 发布出去的对象也是不可变对象，保证了线程的安全性；
	 * 说明：不可变对象，保证了其线程安全性
	 * 
	 * @author root
	 *
	 */
	public class WrapAccountPositionSnapShot {
		/** 账户 **/
		public final String InvestorID;

		/** 合约代码 **/
		public final String InstrumentID;

		/** 昨日持仓 **/
		public final int YdPosition;

		/** 今日持仓 **/
		public final int TdPosition;

		/** 总持仓 **/
		public final int Position;

		/** 买卖 **/
		public final char PosiDirection;

		/** 可平量 **/
		public final int ClosePostion;

		/** 持仓均价 **/
		public final double AvgPrice;

		/** 持仓盈亏 **/
		public final double PositionProfit;

		/** 持仓成本 **/
		public final double PositionCost;

		/** 占用保证金 **/
		public final double UseMargin;

		/** 套利保值 **/
		public final char HedgeFlag;

		/** 品种编号 **/
		public final String ProductID;

		public WrapAccountPositionSnapShot(String investorID,
				String instrumentID, int ydPosition, int tdPosition,
				int position, char posiDirection, int closePostion,
				double avgPrice, double positionProfit, double positionCost,
				double useMargin, char hedgeFlag, String productID) {
			super();
			InvestorID = investorID;
			InstrumentID = instrumentID;
			YdPosition = ydPosition;
			TdPosition = tdPosition;
			Position = position;
			PosiDirection = posiDirection;
			ClosePostion = closePostion;
			AvgPrice = avgPrice;
			PositionProfit = positionProfit;
			PositionCost = positionCost;
			UseMargin = useMargin;
			HedgeFlag = hedgeFlag;
			ProductID = productID;
		}

		@Override
		public boolean equals(Object obj) {
			WrapAccountPositionSnapShot positionSnapShot = (WrapAccountPositionSnapShot) obj;
			if (this.InvestorID.equals(positionSnapShot.InvestorID)
					&& this.InstrumentID.equals(positionSnapShot.InstrumentID)
					&& this.PosiDirection == positionSnapShot.PosiDirection) {
				return true;
			}
			return false;
		}

		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("账户:")
					.append(InvestorID)
					.append("------------合约代码:")
					.append(InstrumentID)
					.append("|")
					.append("总持仓:")
					.append(Position)
					.append("|")
					.append("买卖:")
					.append(PosiDirection)
					// 2-买 3-卖
					.append("|").append("上日持仓:").append(YdPosition).append("|")
					.append("今日持仓:").append(TdPosition).append("|")
					.append("可平量:").append(ClosePostion).append("|")
					.append("持仓成本:").append(PositionCost).append("|")
					.append("持仓均价:").append(AvgPrice).append("|")
					.append("持仓盈亏:").append(PositionProfit).append("|")
					.append("占用的保证金:").append(UseMargin).append("|")
					.append("投保标识:").append(HedgeFlag).append("|")
					.append("交易所标识:").append(HedgeFlag).append("|\n");
			return builder.toString();
		}
	}

	/***
	 * 根据最新获取的持仓信息构造持仓信息
	 * 
	 * @param cThostFtdcInvestorPositionField
	 */
	public WrapAccountPosition(
			CThostFtdcInvestorPositionField cThostFtdcInvestorPositionField) {
		// 合约乘数
		Integer VolumeMultiple = CollectedDataCache.instrumentsMap
				.get(cThostFtdcInvestorPositionField.InstrumentID).VolumeMultiple;
		if (VolumeMultiple == null || VolumeMultiple == 0) {
			throw new IllegalStateException("合约" + entry.InstrumentID
					+ "的基本信息没有初始化 或者 初始化异常");
		}
		String investorID = cThostFtdcInvestorPositionField.InvestorID;
		String instrumentID = cThostFtdcInvestorPositionField.InstrumentID;
		int position = cThostFtdcInvestorPositionField.Position;
		char posiDirection = cThostFtdcInvestorPositionField.PosiDirection;
		int ydPosition = cThostFtdcInvestorPositionField.YdPosition;
		int tdPosition = cThostFtdcInvestorPositionField.TodayPosition;
		int closePostion = (cThostFtdcInvestorPositionField.Position)
				- (cThostFtdcInvestorPositionField.LongFrozen
						+ cThostFtdcInvestorPositionField.ShortFrozen
						+ cThostFtdcInvestorPositionField.CombLongFrozen + cThostFtdcInvestorPositionField.CombShortFrozen);
		double positionCost = cThostFtdcInvestorPositionField.PositionCost;
		double positionProfit = cThostFtdcInvestorPositionField.PositionProfit;
		double avgPrice = 0;
		if (position > 0) {
			avgPrice = cThostFtdcInvestorPositionField.PositionCost / position
					/ VolumeMultiple;
		}
		double useMargin = cThostFtdcInvestorPositionField.UseMargin;
		char hedgeFlag = cThostFtdcInvestorPositionField.HedgeFlag;
		String productID = CollectedDataCache.instrumentsMap
				.get(cThostFtdcInvestorPositionField.InstrumentID).ProductID;
		entry = new WrapAccountPositionSnapShot(investorID, instrumentID,
				ydPosition, tdPosition, position, posiDirection, closePostion,
				avgPrice, positionProfit, positionCost, useMargin, hedgeFlag,
				productID);
	}

	public synchronized void updateByWarpAccountPosition(
			WrapAccountPosition wrapAccountPosition) {
		entry = wrapAccountPosition.getSnapShot();
	}

	/**
	 * 根据行情数据 更新持仓信息（同步锁）
	 * 
	 * @param cThostFtdcDepthMarketDataField
	 */
	public synchronized void updateByDepthMarketDataField(
			CThostFtdcDepthMarketDataField cThostFtdcDepthMarketDataField) {
		// 合约乘数
		Integer VolumeMultiple = CollectedDataCache.instrumentsMap
				.get(entry.InstrumentID).VolumeMultiple;
		// 多头保证金率
		double LongMarginRatio = CollectedDataCache.instrumentsMap
				.get(entry.InstrumentID).LongMarginRatio.get(entry.InvestorID);
		LongMarginRatio = (LongMarginRatio > 1 ? 0 : LongMarginRatio);
		// 空头保证金率
		double ShortMarginRatio = CollectedDataCache.instrumentsMap
				.get(entry.InstrumentID).ShortMarginRatio.get(entry.InvestorID);
		ShortMarginRatio = (ShortMarginRatio > 1 ? 0 : ShortMarginRatio);
		if (VolumeMultiple == null || VolumeMultiple == 0) {
			throw new IllegalStateException("合约" + entry.InstrumentID
					+ "的基本信息没有初始化 或者 初始化异常");
		}
		String investorID = entry.InvestorID;
		String instrumentID = entry.InstrumentID;
		int position = entry.Position;
		char posiDirection = entry.PosiDirection;
		int ydPosition = entry.YdPosition;
		int tdPosition = entry.TdPosition;
		int closePostion = entry.ClosePostion;
		double positionCost = entry.PositionCost;
		// 持仓均价 = 持仓成本/总持仓/合约乘数
		double avgPrice = 0;
		if (position != 0 && VolumeMultiple != 0) {
			avgPrice = positionCost / position / VolumeMultiple;
		}
		// 持仓盈亏
		// （最新价 - 持仓均价）* 持仓总量 * 合约乘数 -- 多头
		// （持仓均价 - 最新价）* 持仓总量 * 合约乘数 -- 空头"
		double positionProfit;
		// 占用保证金 = 单一今持仓*最新价*合约乘数*保证金率 + 单一昨持仓*昨结算价*合约乘数*保证金率
		double useMargin;
		if (entry.PosiDirection == JctpConstants.THOST_FTDC_PD_Long) {// 多头
			positionProfit = (cThostFtdcDepthMarketDataField.LastPrice - avgPrice)
					* position * VolumeMultiple;
			useMargin = (tdPosition * cThostFtdcDepthMarketDataField.LastPrice + ydPosition
					* cThostFtdcDepthMarketDataField.PreSettlementPrice)
					* VolumeMultiple * LongMarginRatio;
		} else {// 空头
			positionProfit = (avgPrice - cThostFtdcDepthMarketDataField.LastPrice)
					* position * VolumeMultiple;
			useMargin = (tdPosition * cThostFtdcDepthMarketDataField.LastPrice + ydPosition
					* cThostFtdcDepthMarketDataField.PreSettlementPrice)
					* VolumeMultiple * ShortMarginRatio;
		}
		char hedgeFlag = entry.HedgeFlag;
		String productID = entry.ProductID;
		entry = new WrapAccountPositionSnapShot(investorID, instrumentID,
				ydPosition, tdPosition, position, posiDirection, closePostion,
				avgPrice, positionProfit, positionCost, useMargin, hedgeFlag,
				productID);
	}

	public String toString() {
		return entry.toString();
	}

	/**
	 * 获得当前持仓信息的快照
	 * 
	 * @return
	 */
	public WrapAccountPositionSnapShot getSnapShot() {
		return entry;
	}

	@Override
	public boolean equals(Object obj) {
		return entry.equals(((WrapAccountPosition) obj).getSnapShot());
	}

}
