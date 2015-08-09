package com.xun360.guojin.dataexchange.model;

import java.util.HashMap;
import java.util.Map;

import net.jctp.CThostFtdcInstrumentField;
import net.jctp.JctpConstants;

public class InstrumentDetialForInvestor extends CThostFtdcInstrumentField {

	/**
	 * 多头保证金率
	 * key为投资者ID
	 * @see JctpConstants#SizeOf_TThostFtdcRatioType
	 */
	public Map<String, Double> LongMarginRatio = new HashMap<String, Double>();

	/**
	 * 空头保证金率
	 * key为投资者ID
	 * @see JctpConstants#SizeOf_TThostFtdcRatioType
	 */
	public Map<String, Double> ShortMarginRatio = new HashMap<String, Double>();

	public InstrumentDetialForInvestor(CThostFtdcInstrumentField cThostFtdcInstrumentField) {
		super.InstrumentID=cThostFtdcInstrumentField.InstrumentID;
		super.ExchangeID=cThostFtdcInstrumentField.ExchangeID;
		super.InstrumentName=cThostFtdcInstrumentField.InstrumentName;
		super.ExchangeInstID=cThostFtdcInstrumentField.ExchangeInstID;
		super.ProductID=cThostFtdcInstrumentField.ProductID;
		super.ProductClass=cThostFtdcInstrumentField.ProductClass;
		super.DeliveryYear=cThostFtdcInstrumentField.DeliveryYear;
		super.DeliveryMonth=cThostFtdcInstrumentField.DeliveryMonth;
		super.MaxMarketOrderVolume=cThostFtdcInstrumentField.MaxMarketOrderVolume;
		super.MinMarketOrderVolume=cThostFtdcInstrumentField.MinMarketOrderVolume;
		super.MaxLimitOrderVolume=cThostFtdcInstrumentField.MaxLimitOrderVolume;
		super.MinLimitOrderVolume=cThostFtdcInstrumentField.MinLimitOrderVolume;
		super.VolumeMultiple=cThostFtdcInstrumentField.VolumeMultiple;
		super.PriceTick=cThostFtdcInstrumentField.PriceTick;
		super.CreateDate=cThostFtdcInstrumentField.CreateDate;
		super.OpenDate=cThostFtdcInstrumentField.OpenDate;
		super.ExpireDate=cThostFtdcInstrumentField.ExpireDate;
		super.StartDelivDate=cThostFtdcInstrumentField.StartDelivDate;
		super.EndDelivDate=cThostFtdcInstrumentField.EndDelivDate;
		super.InstLifePhase=cThostFtdcInstrumentField.InstLifePhase;
		super.IsTrading=cThostFtdcInstrumentField.IsTrading;
		super.PositionType=cThostFtdcInstrumentField.PositionType;
		super.PositionDateType=cThostFtdcInstrumentField.PositionDateType;
		super.LongMarginRatio=cThostFtdcInstrumentField.LongMarginRatio;
		super.ShortMarginRatio=cThostFtdcInstrumentField.ShortMarginRatio;
		super.MaxMarginSideAlgorithm=cThostFtdcInstrumentField.MaxMarginSideAlgorithm;
		super.UnderlyingInstrID=cThostFtdcInstrumentField.UnderlyingInstrID;
		super.StrikePrice=cThostFtdcInstrumentField.StrikePrice;
		super.OptionsType=cThostFtdcInstrumentField.OptionsType;
		super.UnderlyingMultiple=cThostFtdcInstrumentField.UnderlyingMultiple;
		super.CombinationType=cThostFtdcInstrumentField.CombinationType;
	}
	
	
	
}
