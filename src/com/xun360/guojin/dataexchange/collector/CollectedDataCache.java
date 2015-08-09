package com.xun360.guojin.dataexchange.collector;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import net.jctp.CThostFtdcDepthMarketDataField;

import com.xun360.guojin.dataexchange.model.InstrumentDetialForInvestor;
import com.xun360.guojin.dataexchange.model.WrapAccountPosition;
import com.xun360.guojin.dataexchange.model.WrapAccountTradingMoney;

/**
 * 收集數據緩存
 * 
 * @author jw
 * 
 */
public class CollectedDataCache {

	/** 合约列表Map **/
	/** key=合约号，value=合約詳細信息 **/
	public final static ConcurrentHashMap<String, InstrumentDetialForInvestor> instrumentsMap 
				= new ConcurrentHashMap<String, InstrumentDetialForInvestor>();

	/** 当前的最新行情数据 **/
	/** key=合约号，value=深度行情 **/
	public final static ConcurrentHashMap<String, CThostFtdcDepthMarketDataField> instrumentsMarketDataMap 
				= new ConcurrentHashMap<String, CThostFtdcDepthMarketDataField>();
	
	/** 当前用户持仓情况 **/
	/** key=用户帐号，value=List<持仓详情> **/
	public final static ConcurrentHashMap<String, List<WrapAccountPosition>> accountPositionDataMap
				= new ConcurrentHashMap<String, List<WrapAccountPosition>>();
	
	/** 当前用户资金情况 **/
	/** key=用户帐号，value=资金情况 **/
	public final static ConcurrentHashMap<String, WrapAccountTradingMoney> accountMoneyDataMap
				= new ConcurrentHashMap<String, WrapAccountTradingMoney>();
	
}
