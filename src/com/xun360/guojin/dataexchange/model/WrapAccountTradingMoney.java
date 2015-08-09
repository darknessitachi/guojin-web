package com.xun360.guojin.dataexchange.model;

import java.util.List;

import com.xun360.guojin.dataexchange.model.WrapAccountPosition.WrapAccountPositionSnapShot;

import net.jctp.CThostFtdcTradingAccountField;

/**
 * 持仓信息包装对象
 * 
 * @author root
 *
 */
public class WrapAccountTradingMoney {

	private volatile WrapAccountTradingMoneySnapShot entry;

	/**
	 * 快照对象 说明：不可变对象，保证了其线程安全性
	 * 
	 * @author root
	 */
	public class WrapAccountTradingMoneySnapShot {

		/** 账户 **/
		public final String InvestorID;

		/** 上次结算准备金（静态权益） **/
		public final double PreBalance;

		/** 动态权益 **/
		public final double DynamicRight;

		/** 当天最高动态权益 **/
		public final double TdHigestDynamicRight;

		/** 持仓盈亏 **/
		public final double PositionProfit;

		/** 平仓盈亏 **/
		public final double CloseProfit;

		/** 占用保证金 **/
		public final double CurrMargin;

		/** 可用资金 **/
		public final double AvailableCash;

		/** 下单冻结 **/
		public final double FrozenSubmit;

		/** 入金 **/
		public final double Deposit;

		/** 出金 **/
		public final double Withdraw;

		/** 上次信用额度 **/
		public final double PreCredit;

		/** 上次质押 **/
		public final double PreMortgage;

		/** 质押 **/
		public final double Mortgage;

		/** 手续费 **/
		public final double Commission;

		/** 风险度 **/
		public final double RiskRate;

		/** 冻结保证金 **/
		public final double FrozenMargin;

		/** 冻结手续费 **/
		public final double FrozenCommission;

		/** 交易日 **/
		public final String TradingDay;

		public WrapAccountTradingMoneySnapShot(String investorID,
				double preBalance, double dynamicRight,
				double tdHigestDynamicRight, double positionProfit,
				double closeProfit, double currMargin, double availableCash,
				double frozenSubmit, double deposit, double withdraw,
				double preCredit, double preMortgage, double mortgage,
				double commission, double riskRate, double frozenMargin,
				double frozenCommission, String tradingDay) {
			super();
			InvestorID = investorID;
			PreBalance = preBalance;
			DynamicRight = dynamicRight;
			TdHigestDynamicRight = tdHigestDynamicRight;
			PositionProfit = positionProfit;
			CloseProfit = closeProfit;
			CurrMargin = currMargin;
			AvailableCash = availableCash;
			FrozenSubmit = frozenSubmit;
			Deposit = deposit;
			Withdraw = withdraw;
			PreCredit = preCredit;
			PreMortgage = preMortgage;
			Mortgage = mortgage;
			Commission = commission;
			RiskRate = riskRate;
			FrozenMargin = frozenMargin;
			FrozenCommission = frozenCommission;
			TradingDay = tradingDay;
		}

		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("账户:").append(InvestorID)
					.append("------------静态权益:").append(PreBalance).append("|")
					.append("动态权益:").append(DynamicRight).append("|")
					.append("持仓盈亏:").append(PositionProfit).append("|")
					.append("平仓盈亏:").append(CloseProfit).append("|")
					.append("占用保证金:").append(CurrMargin).append("|")
					.append("可用资金:").append(AvailableCash).append("|")
					.append("下单冻结:").append(FrozenSubmit).append("|")
					.append("风险度(%):").append(RiskRate * 100).append("%|");
			return builder.toString();
		}
	}

	/**
	 * 根据CThostFtdcTradingAccountField对象构造
	 * 
	 * @param cThostFtdcTradingAccountField
	 */
	public WrapAccountTradingMoney(
			CThostFtdcTradingAccountField cThostFtdcTradingAccountField) {
		// entry = new WrapAccountTradingMoneySnapShot();
		String investorID = cThostFtdcTradingAccountField.AccountID;
		double preBalance = cThostFtdcTradingAccountField.PreBalance;
		// 构造时 最高动态权益 就是 当前的动态权益
		double tdHigestDynamicRight = cThostFtdcTradingAccountField.Balance;
		double dynamicRight = cThostFtdcTradingAccountField.Balance;
		double positionProfit = cThostFtdcTradingAccountField.PositionProfit;
		double closeProfit = cThostFtdcTradingAccountField.CloseProfit;
		double currMargin = cThostFtdcTradingAccountField.CurrMargin;
		double availableCash = cThostFtdcTradingAccountField.Available;
		// 下单冻结 = 动态权益 - 可用资金 - 占用保证金
		double frozenSubmit = dynamicRight - availableCash - currMargin;
		if (frozenSubmit < 0 || (0 < frozenSubmit && frozenSubmit < 1)) {
			frozenSubmit = 0;
		}
		/** 入金 **/
		double deposit = cThostFtdcTradingAccountField.Deposit;
		/** 出金 **/
		double withdraw = cThostFtdcTradingAccountField.Withdraw;
		/** 上次信用额度 **/
		double preCredit = cThostFtdcTradingAccountField.PreCredit;
		/** 上次质押 **/
		double preMortgage = cThostFtdcTradingAccountField.PreMortgage;
		/** 质押 **/
		double mortgage = cThostFtdcTradingAccountField.Mortgage;
		/** 手续费 **/
		double commission = cThostFtdcTradingAccountField.Commission;
		/** 冻结保证金 **/
		double frozenMargin = cThostFtdcTradingAccountField.FrozenMargin;
		/** 冻结手续费 **/
		double frozenCommission = cThostFtdcTradingAccountField.FrozenCommission;
		// 风险度 = 占用保证金/可用资金
		double riskRate = (availableCash == 0.0 ? 0 : currMargin
				/ availableCash);
		String tradingDay = cThostFtdcTradingAccountField.TradingDay;
		entry = new WrapAccountTradingMoneySnapShot(investorID, preBalance,
				dynamicRight, tdHigestDynamicRight, positionProfit,
				closeProfit, currMargin, availableCash, frozenSubmit, deposit,
				withdraw, preCredit, preMortgage, mortgage, commission,
				riskRate, frozenMargin, frozenCommission, tradingDay);
	}

	/**
	 * 根据资金数据来更新 当前资金情况
	 * 
	 * @param cThostFtdcTradingAccountField
	 */
	public synchronized void updateByTradingAccountField(
			CThostFtdcTradingAccountField cThostFtdcTradingAccountField) {
		String investorID = cThostFtdcTradingAccountField.AccountID;
		double preBalance = cThostFtdcTradingAccountField.PreBalance;
		// 更新当天最高动态权益
		double tdHigestDynamicRight = 0;
		if (entry.TradingDay.equals(cThostFtdcTradingAccountField.TradingDay)) {// 最高动态权益
																				// 只记录今天的
			tdHigestDynamicRight = entry.TdHigestDynamicRight > cThostFtdcTradingAccountField.Balance ? entry.TdHigestDynamicRight
					: cThostFtdcTradingAccountField.Balance;
		} else {
			tdHigestDynamicRight = cThostFtdcTradingAccountField.Balance;
		}
		double dynamicRight = cThostFtdcTradingAccountField.Balance;
		double positionProfit = cThostFtdcTradingAccountField.PositionProfit;
		double closeProfit = cThostFtdcTradingAccountField.CloseProfit;
		double currMargin = cThostFtdcTradingAccountField.CurrMargin;
		double availableCash = cThostFtdcTradingAccountField.Available;
		// 下单冻结 = 动态权益 - 可用资金 - 占用保证金
		double frozenSubmit = dynamicRight - availableCash - currMargin;
		if (frozenSubmit < 0 || (0 < frozenSubmit && frozenSubmit < 1)) {
			frozenSubmit = 0;
		}
		// 风险度 = 占用保证金/动态权益
		double riskRate = (dynamicRight == 0.0 ? 0 : currMargin
				/ dynamicRight);
		/** 入金 **/
		double deposit = entry.Deposit;
		/** 出金 **/
		double withdraw = entry.Withdraw;
		/** 上次信用额度 **/
		double preCredit = entry.PreCredit;
		/** 上次质押 **/
		double preMortgage = entry.PreMortgage;
		/** 质押 **/
		double mortgage = entry.Mortgage;
		/** 手续费 **/
		double commission = entry.Commission;
		/** 冻结保证金 **/
		double frozenMargin = entry.FrozenMargin;
		/** 冻结手续费 **/
		double frozenCommission = entry.FrozenCommission;
		String tradingDay = cThostFtdcTradingAccountField.TradingDay;
		entry = new WrapAccountTradingMoneySnapShot(investorID, preBalance,
				dynamicRight, tdHigestDynamicRight, positionProfit,
				closeProfit, currMargin, availableCash, frozenSubmit, deposit,
				withdraw, preCredit, preMortgage, mortgage, commission,
				riskRate, frozenMargin, frozenCommission, tradingDay);
	}

	/**
	 * 根据当前的持仓来更新 当前资金情况
	 * 
	 * @param cThostFtdcTradingAccountField
	 */
	public synchronized void updateByWrapAccountPosition(
			List<WrapAccountPositionSnapShot> wrapAccountPositions) {
		String investorID = entry.InvestorID;
		double preBalance = entry.PreBalance;
		// 占用保证及 = 各持仓保证金 之和
		double currMargin = 0;
		// 持仓盈亏 = 各持仓盈亏 之和
		double positionProfit = 0;
		for (WrapAccountPositionSnapShot accountPositionSnapShot : wrapAccountPositions) {
			currMargin = accountPositionSnapShot.UseMargin + currMargin;
			positionProfit = accountPositionSnapShot.PositionProfit
					+ positionProfit;
		}
		// 动态权益 = 上次结算准备金+平仓盈亏+持仓盈亏（各持仓）-手续费+入金-出金-上次信用额度-上次质押+质押
		double dynamicRight = entry.PreBalance + entry.CloseProfit
				+ positionProfit - entry.Commission + entry.Deposit
				- entry.Withdraw - entry.PreCredit - entry.PreMortgage
				+ entry.Mortgage;
		// 更新当天最高动态权益
		double tdHigestDynamicRight = entry.TdHigestDynamicRight > dynamicRight ? entry.TdHigestDynamicRight
				: dynamicRight;
		double closeProfit = entry.CloseProfit;
		// * 可用资金：动态权益 - 被冻结的已经不可用的资金（保证金，冻结保证金，冻结手续费）
		double availableCash = dynamicRight - currMargin - entry.FrozenMargin
				- entry.FrozenCommission;
		// 下单冻结 = 动态权益 - 可用资金 - 占用保证金
		double frozenSubmit = dynamicRight - availableCash - currMargin;
		if (frozenSubmit < 0 || (0 < frozenSubmit && frozenSubmit < 1)) {
			frozenSubmit = 0;
		}
		// 风险度 = 占用保证金/动态权益
		double riskRate = (availableCash == 0.0 ? 0 : currMargin / dynamicRight);
		/** 入金 **/
		double deposit = entry.Deposit;
		/** 出金 **/
		double withdraw = entry.Withdraw;
		/** 上次信用额度 **/
		double preCredit = entry.PreCredit;
		/** 上次质押 **/
		double preMortgage = entry.PreMortgage;
		/** 质押 **/
		double mortgage = entry.Mortgage;
		/** 手续费 **/
		double commission = entry.Commission;
		/** 冻结保证金 **/
		double frozenMargin = entry.FrozenMargin;
		/** 冻结手续费 **/
		double frozenCommission = entry.FrozenCommission;
		String tradingDay = entry.TradingDay;
		entry = new WrapAccountTradingMoneySnapShot(investorID, preBalance,
				dynamicRight, tdHigestDynamicRight, positionProfit,
				closeProfit, currMargin, availableCash, frozenSubmit, deposit,
				withdraw, preCredit, preMortgage, mortgage, commission,
				riskRate, frozenMargin, frozenCommission, tradingDay);

	}

	/**
	 * 获得当前资金信息的快照
	 * 
	 * @return
	 */
	public WrapAccountTradingMoneySnapShot getSnapShot() {
		return entry;
	}

	public String toString() {
		return entry.toString();
	}
}
