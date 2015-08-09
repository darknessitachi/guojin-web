package com.xun360.guojin.dataexchange.opr;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import net.jctp.CThostFtdcDepthMarketDataField;
import net.jctp.CThostFtdcInputOrderField;
import net.jctp.CThostFtdcInstrumentField;
import net.jctp.JctpConstants;
import net.jctp.JctpException;

import org.apache.log4j.Logger;
import org.springframework.util.Assert;

import com.xun360.guojin.dataexchange.cfg.ExchangeConfig;
import com.xun360.guojin.dataexchange.collector.AccountPositionCollector;
import com.xun360.guojin.dataexchange.collector.AssistDataCache;
import com.xun360.guojin.dataexchange.collector.CollectedDataCache;
import com.xun360.guojin.dataexchange.conductor.CollectJobConductor;
import com.xun360.guojin.dataexchange.exception.ClosePosiException;
import com.xun360.guojin.dataexchange.exception.CollectDataException;
import com.xun360.guojin.dataexchange.model.ClosePosiStat;
import com.xun360.guojin.dataexchange.model.InstrumentDetialForInvestor;
import com.xun360.guojin.dataexchange.model.WrapAccountPosition;
import com.xun360.guojin.dataexchange.model.WrapAccountPosition.WrapAccountPositionSnapShot;
import com.xun360.guojin.dataexchange.model.WrapAccountTradingMoney;
import com.xun360.guojin.dataexchange.model.WrapAccountTradingMoney.WrapAccountTradingMoneySnapShot;

public class ClosePositionOpr {

	private static final Logger log = Logger.getLogger(ClosePositionOpr.class);
	
	private static final String SHFE = "SHFE";//上期所代码
	
//	private static final int maxCloseVolumeOnce = 100;//最大一个报单的交易手数
	
	/**
	 * 全平 接口一
	 * @param investor 投资者帐号
	 * @param instrumnents 合约号集合 
	 * @param directType 多空方向 买 或者 卖（2-买，3-卖）
	 * @throws ClosePosiException 
	 */
	public static List<ClosePosiStat> fullClosePosi(String investor, List<String> instrumnents,
			char directType) throws ClosePosiException {
		return percentClosePosi(1, investor, instrumnents, directType);
	}

	/**
	 * 全平 接口二
	 * @param investor 投资者帐号
	 * @param instrumnents 合约号集合 
	 * @throws ClosePosiException 
	 */
	public static List<ClosePosiStat> fullClosePosi(String investor, List<String> instrumnents)
			throws ClosePosiException {
		return percentClosePosi(1, investor, instrumnents);
	}

	/**
	 * 全平 接口三
	 * @param investor 投资者帐号
	 * @param directType 多空方向 买 或者 卖（2-买，3-卖）
	 * @throws ClosePosiException 
	 */
	public static List<ClosePosiStat> fullClosePosi(String investor, char directType)
			throws ClosePosiException {
		return percentClosePosi(1, investor, directType);
	}

	/**
	 * 全平 接口四
	 * @param investor 投资者帐号
	 * @throws ClosePosiException 
	 */
	public static List<ClosePosiStat> fullClosePosi(String investor) throws ClosePosiException {
		return percentClosePosi(1, investor);
	}

	/**
	 * 按 静态权益 百分比平仓
	 * @param percent 百分比 70% == 0.7
	 * @param investor 投资者帐号
	 * @param instrumnents 合约号集合
	 * @param directType 合约多空方向 买 或者 卖（2-买，3-卖）
	 * @throws ClosePosiException 
	 */
	public static List<ClosePosiStat> percentClosePosi(double percent, String investor,
			List<String> instrumnents, char directType)
			throws ClosePosiException {
		try {
			Assert.isTrue(percent > 0 && percent <= 1);
			Assert.notNull(investor);
			Assert.notNull(instrumnents);
			Assert.notEmpty(instrumnents);
		} catch (Exception e) {
			throw new ClosePosiException(e.getMessage());
		}
		return percentClose(percent, investor, instrumnents,
				String.valueOf(directType));
	}

	/**
	 * 按 静态权益 百分比平仓
	 * @param percent 百分比 70% == 0.7
	 * @param investor 投资者帐号
	 * @param directType 合约多空方向 买 或者 卖（2-买，3-卖）
	 * @throws ClosePosiException 
	 */
	public static List<ClosePosiStat> percentClosePosi(double percent, String investor,
			char directType) throws ClosePosiException {
		try {
			Assert.isTrue(percent > 0 && percent <= 1);
			Assert.notNull(investor);
		} catch (Exception e) {
			throw new ClosePosiException(e.getMessage());
		}
		return percentClose(percent, investor, null, String.valueOf(directType));
	}

	/**
	 * 按 静态权益 百分比平仓
	 * @param percent 百分比 70% == 0.7
	 * @param investor 投资者帐号
	 * @param instrumnents 合约号集合
	 * @throws ClosePosiException 
	 */
	public static List<ClosePosiStat> percentClosePosi(double percent, String investor,
			List<String> instrumnents) throws ClosePosiException {
		try {
			Assert.isTrue(percent > 0 && percent <= 1);
			Assert.notNull(investor);
			Assert.notNull(instrumnents);
			Assert.notEmpty(instrumnents);
		} catch (Exception e) {
			throw new ClosePosiException(e.getMessage());
		}
		return percentClose(percent, investor, instrumnents, null);
	}

	/**
	 * 按 静态权益 百分比平仓
	 * @param percent 百分比 70% == 0.7
	 * @param investor 投资者帐号
	 * @return 
	 * @throws ClosePosiException 
	 */
	public static List<ClosePosiStat> percentClosePosi(double percent, String investor)
			throws ClosePosiException {
		try {
			Assert.isTrue(percent > 0 && percent <= 1);
			Assert.notNull(investor);
		} catch (Exception e) {
			throw new ClosePosiException(e.getMessage());
		}
		return percentClose(percent, investor, null, null);
	}

	private static List<ClosePosiStat> percentClose(double percent, String investor,
			List<String> instrumnents, String directType)
			throws ClosePosiException {
		try {
			List<CThostFtdcInputOrderField> closeOprReqs = new ArrayList<CThostFtdcInputOrderField>();
			// 首先根据投资者的id，获取持仓信息 和 对应的资金信息
			List<WrapAccountPosition> accountPositions = CollectedDataCache.accountPositionDataMap
					.get(investor);
			WrapAccountTradingMoneySnapShot accountTradingMoney = CollectedDataCache.accountMoneyDataMap
					.get(investor).getSnapShot();
			List<WrapAccountPositionSnapShot> positionSnapShots = new ArrayList<WrapAccountPosition.WrapAccountPositionSnapShot>();
			for (WrapAccountPosition accountPosition : accountPositions) {
				positionSnapShots.add(accountPosition.getSnapShot());
			}
			if (accountTradingMoney == null || positionSnapShots.size() == 0) {
				throw new ClosePosiException("由于账户" + investor
						+ "的持仓缓存信息 或者 资金缓存信息 为空");
			}
			// 根据 资金的 【静态权益】*percent = 【这次平仓的最大平仓金额】（maxCloseCash）
			double maxCloseCash = accountTradingMoney.PreBalance * percent;
			// 将所有持仓按 【持仓盈亏】 从低到高 排序
			Collections.sort(positionSnapShots, new PosiComparator(
					PosiComparator.Director.ASC));
			// 遍历 持仓列表 按1手 为最小单位 逐一计算
			// 合约占用保证金（【合约最新价】*【可平量】*【合约乘数】*【保证金率】） < 【这次平仓的最大平仓金额】 如果成立，继续平仓
			for (WrapAccountPositionSnapShot accountPositionSnapShot : positionSnapShots) {
				if (accountPositionSnapShot.ClosePostion > 0) {
					if (instrumnents != null && instrumnents.size() > 0
							&& !instrumnents.contains(accountPositionSnapShot.InstrumentID)) {
						continue;
					}
					if (directType != null && !directType
									.equals(String.valueOf(accountPositionSnapShot.PosiDirection))) {
						continue;
					}
					if(maxCloseCash <= 0){
						break;
					}
					if (maxCloseCash >= getUseMargin(accountPositionSnapShot,accountPositionSnapShot.ClosePostion)) {
						// 平仓提交
						closeOprReqs.addAll(submitTradeReq(
								ExchangeConfig.BROKER_ID,
								investor,
								accountPositionSnapShot.InstrumentID,
								null,
								!(accountPositionSnapShot.PosiDirection == JctpConstants.THOST_FTDC_PD_Long),
								false, accountPositionSnapShot.Position,
								getClosePosiPrice(accountPositionSnapShot)));
						// 最大平仓金额 更新
						maxCloseCash = maxCloseCash
								- getUseMargin(accountPositionSnapShot,accountPositionSnapShot.ClosePostion);
						// 继续平仓
					} else {
						for (int curClosePosi = accountPositionSnapShot.ClosePostion - 1; curClosePosi >= 0; curClosePosi--) {// 逐手递减
							if (maxCloseCash > 0 && maxCloseCash >= getUseMargin(accountPositionSnapShot,curClosePosi)) {
								// 平仓提交
								closeOprReqs.addAll(submitTradeReq(
										ExchangeConfig.BROKER_ID,
										investor,
										accountPositionSnapShot.InstrumentID,
										null,
										!(accountPositionSnapShot.PosiDirection == JctpConstants.THOST_FTDC_PD_Long),
										false,
										(curClosePosi == 0 ? curClosePosi+1 : curClosePosi),
										getClosePosiPrice(accountPositionSnapShot)));
								// 最大平仓金额 更新
								maxCloseCash = maxCloseCash
										- getUseMargin(accountPositionSnapShot,(curClosePosi == 0 ? curClosePosi+1 : curClosePosi));
								// 跳出 手数递减循环 继续平仓
								break;
							}
						}
					}
				}
			}
			// 同步平仓执行 返回 平仓结果统计
			SyncClosePosi closePosi = new SyncClosePosi(closeOprReqs);
			List<ClosePosiStat> closePosiStats = closePosi.syncClose();
			// 平仓完成后，调用仓位获取 和 资金情况获取
			// 更新持仓
			// 更新资金信息
			syncInvestorCashAndPosi(investor);
			log.info("------------------账户【" + investor + "】的平仓操作成交统计------------------\n" + closePosiStats);
			return closePosiStats;
		} catch (Exception e) {
			String message = "对账户【" + investor + "】的平仓操作失败；异常信息："
					+ e.getMessage();
			log.error(message,e);
			e.printStackTrace();
			ClosePosiException closePosiException = new ClosePosiException(message);
			closePosiException.addSuppressed(e);
			throw closePosiException;
		} 
	}

	// 合约占用保证金（【持仓最新价】*【可平量】*【合约乘数】*【保证金率】）
	private static double getUseMargin(
			WrapAccountPositionSnapShot accountPositionSnapShot,int closePosi){
		CThostFtdcDepthMarketDataField cThostFtdcDepthMarketDataField = CollectedDataCache.instrumentsMarketDataMap.get(accountPositionSnapShot.InstrumentID);
		InstrumentDetialForInvestor cThostFtdcInstrumentField = CollectedDataCache.instrumentsMap
				.get(accountPositionSnapShot.InstrumentID);
		int VolumeMultiple = cThostFtdcInstrumentField.VolumeMultiple;
//		double LongMarginRatio = cThostFtdcInstrumentField.LongMarginRatio.get(accountPositionSnapShot.InvestorID) > 1 ? 0 : cThostFtdcInstrumentField.LongMarginRatio.get(accountPositionSnapShot.InvestorID);
		double MarginRatio = (accountPositionSnapShot.PosiDirection == JctpConstants.THOST_FTDC_PD_Long ? cThostFtdcInstrumentField.LongMarginRatio.get(accountPositionSnapShot.InvestorID)
				: cThostFtdcInstrumentField.ShortMarginRatio.get(accountPositionSnapShot.InvestorID));
		MarginRatio = MarginRatio > 1 ? 1 : MarginRatio;
		if(cThostFtdcDepthMarketDataField == null){
			log.warn("计算持仓保证金时，找不到合约[" + accountPositionSnapShot.InstrumentID + "]对应的行情数据，只能采用持仓数据的占用保证金");
			return accountPositionSnapShot.UseMargin;
		}else{
			return cThostFtdcDepthMarketDataField.LastPrice
					* closePosi * VolumeMultiple
					* MarginRatio;
		}
	}

	/**
	 * 仓位 持仓盈利 比较器
	 * @author root
	 */
	private static class PosiComparator implements
			Comparator<WrapAccountPositionSnapShot> {

		private static enum Director {
			ASC(-1), DESC(1);

			private int sortType;

			private Director(int sortType) {
				this.sortType = sortType;
			}

			public int getSortType() {
				return sortType;
			}
		}

		private final Director sortType;

		public PosiComparator(Director sortType) {
			super();
			this.sortType = sortType;
		}

		@Override
		public int compare(WrapAccountPositionSnapShot o1,
				WrapAccountPositionSnapShot o2) {
			if (o1.PositionProfit < o2.PositionProfit) {
				return sortType.getSortType();
			} else if (o1.PositionProfit > o2.PositionProfit) {
				return -sortType.getSortType();
			}
			return 0;
		}

	}


	/**
	 * 获得平仓价格
	 * 
	 * @param snapShot
	 * @return
	 * @throws ClosePosiException
	 */
	private static double getClosePosiPrice(WrapAccountPositionSnapShot snapShot)
			throws ClosePosiException {
		double closePrice = 0;
		CThostFtdcDepthMarketDataField cThostFtdcDepthMarketDataField = CollectedDataCache.instrumentsMarketDataMap
				.get(snapShot.InstrumentID);
		if (cThostFtdcDepthMarketDataField != null) {
			closePrice = (snapShot.PosiDirection == JctpConstants.THOST_FTDC_PD_Long ? CollectedDataCache.instrumentsMarketDataMap
					.get(snapShot.InstrumentID).LowerLimitPrice
					: CollectedDataCache.instrumentsMarketDataMap
							.get(snapShot.InstrumentID).UpperLimitPrice);
		} else {
			String message = "因没有找到账户【" + snapShot.InvestorID + "】的对应合约["
					+ snapShot.InstrumentID + "]的行情信息，无法执行平仓操作；仓位信息："
					+ snapShot;
			log.error(message);
			throw new ClosePosiException(message);
		}
		return closePrice;
	}

	/**
	 * 提交交易请求
	 * @param brokerId
	 * @param investorId
	 * @param instrumentId
	 * @param orderRef
	 * @param direction
	 * @param open
	 * @param volume
	 * @param price
	 * @throws JctpException
	 * @throws ClosePosiException
	 */
	private static List<CThostFtdcInputOrderField> submitTradeReq(
			String brokerId, String investorId, String instrumentId,
			String orderRef, boolean direction, boolean open, int volume,
			double price) {
		final int maxCloseVolumeOnce = 100;
		List<CThostFtdcInputOrderField> closeOprReqs = new ArrayList<CThostFtdcInputOrderField>();
		WrapAccountPositionSnapShot currentSolPosi = null;
		int YdPosition = 0;//昨仓
		int TdPosition = 0;//今仓
		InstrumentDetialForInvestor cThostFtdcInstrumentField = CollectedDataCache.instrumentsMap.get(instrumentId);
		if(SHFE.equals(cThostFtdcInstrumentField.ExchangeID)){//如果是上期所
			//需要区分平今 和 平仓
			List<WrapAccountPosition> accountPositions = CollectedDataCache.accountPositionDataMap.get(investorId);
			for(WrapAccountPosition accountPosition : accountPositions){
				WrapAccountPositionSnapShot snapShot = accountPosition.getSnapShot();
				if(snapShot.InstrumentID.equals(instrumentId) 
						&& ((snapShot.PosiDirection == JctpConstants.THOST_FTDC_PD_Long && !direction) 
								|| (snapShot.PosiDirection == JctpConstants.THOST_FTDC_PD_Short && direction))){
					//买卖方向 和 合约id都相同
					currentSolPosi = snapShot;
					break;
				}
			}
			if(currentSolPosi == null){
				return closeOprReqs;
			}
			YdPosition = currentSolPosi.YdPosition;
			TdPosition = currentSolPosi.TdPosition;
		}
		while (volume > 0) {
			String closeType = JctpConstants.STRING_THOST_FTDC_OF_Close;//平仓
			int closeVol = (volume  < maxCloseVolumeOnce ? volume : maxCloseVolumeOnce);
			if(SHFE.equals(cThostFtdcInstrumentField.ExchangeID)){//如果是上期所合约
				//优先今平 //然后平仓
				if(TdPosition > 0){//今平优先
					closeVol = Math.min(volume, Math.min(maxCloseVolumeOnce, TdPosition));
					TdPosition = TdPosition - closeVol;
					closeType = JctpConstants.STRING_THOST_FTDC_OF_CloseToday;//平今
				}else if(YdPosition > 0){//平仓滞后(昨仓)
					closeVol = Math.min(volume, Math.min(maxCloseVolumeOnce, YdPosition));
					YdPosition = YdPosition - closeVol;
				}else{
					break;
				}
			}else{//其它 交易所的合约 
				closeVol = Math.min(maxCloseVolumeOnce,volume);
			}
			volume = volume - closeVol;
				
			CThostFtdcInputOrderField r = new CThostFtdcInputOrderField();
			r.BrokerID = brokerId;
			r.InvestorID = investorId;
			r.InstrumentID = instrumentId;
			r.OrderRef = orderRef;
			r.UserID = investorId;
			r.MinVolume = 1;
			r.ForceCloseReason = JctpConstants.THOST_FTDC_FCC_NotForceClose; // 强平原因:
			// 非强平
			r.IsAutoSuspend = false; // 自动挂起标志: 不挂起
			r.UserForceClose = false; // 用户强评标志: 否
			r.TimeCondition = JctpConstants.THOST_FTDC_TC_GFD; // 当日有效
			r.StopPrice = 0; // 止损价
			r.OrderPriceType = JctpConstants.THOST_FTDC_OPT_LimitPrice; // 限价
			r.LimitPrice = price;
			r.Direction = direction ? JctpConstants.THOST_FTDC_D_Buy
					: JctpConstants.THOST_FTDC_D_Sell; // 买卖标志
			r.CombOffsetFlag = open ? JctpConstants.STRING_THOST_FTDC_OF_Open
					: closeType; // 开平标志
			r.CombHedgeFlag = JctpConstants.STRING_THOST_FTDC_HF_Speculation; // 投机
			r.ContingentCondition = JctpConstants.THOST_FTDC_CC_Immediately; // 立即触发
			r.VolumeCondition = JctpConstants.THOST_FTDC_VC_AV; // 任意数量成交
			r.VolumeTotalOriginal = closeVol; // 数量
			closeOprReqs.add(r);
			log.debug("注册平仓操作……对账户[" + investorId + "]的合约号为[" + instrumentId + "]的["
					+ (direction ? "买" : "卖") + "]合约[" + (open ? "开仓" : (closeType == JctpConstants.STRING_THOST_FTDC_OF_CloseToday? "平今":"平仓"))
					+ "][" + closeVol + "]手");
		}
		return closeOprReqs;
	}
	
	/**
	 * 同步更新 账户 资金 和 持仓
	 * @param investor
	 */
	public static void syncInvestorCashAndPosi(String investor){
		// 更新持仓
		try {
			new AccountPositionCollector(AssistDataCache.accountMap.get(investor))
					.collect();
			List<WrapAccountPosition> accountPositions = CollectedDataCache.accountPositionDataMap
					.get(investor);
			List<WrapAccountPositionSnapShot> accountPositionSnapShots = new ArrayList<WrapAccountPosition.WrapAccountPositionSnapShot>();
			for (WrapAccountPosition accountPosition : accountPositions) {
				accountPositionSnapShots.add(accountPosition.getSnapShot());
			}
			// 更新资金信息
			WrapAccountTradingMoney accountTradingMoney = CollectedDataCache.accountMoneyDataMap
					.get(investor);
			accountTradingMoney
					.updateByWrapAccountPosition(accountPositionSnapShots);
			log.info("对账户【" + investor + "】的平仓操作结束\n仓位信息：\n" + accountPositions
					+ "资金信息：\n" + accountTradingMoney);
		} catch (CollectDataException e) {
			String message = "对账户【" + investor + "】的更新资金信息和持仓信息失败；异常信息："
					+ e.getMessage();
			log.error(message);
		}
	}

	public static void main(String[] args) throws InterruptedException,
			ClosePosiException, CollectDataException {
//		CollectJobConductor.initBaseData();
//		CollectJobConductor.startCollect();
//		Thread.sleep(5000);
//		ClosePositionOpr.percentClosePosi(1, "00000008");
		// closePositionOpr.fullClosePosi("00000099");
		percentClosePosi(0, "00000099",
				Collections.EMPTY_LIST, '1');
	}

}
