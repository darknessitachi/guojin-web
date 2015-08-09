package com.xun360.guojin.dataexchange.collector;

import java.io.IOException;
import java.util.Properties;

import net.jctp.CThostFtdcInstrumentField;
import net.jctp.CThostFtdcQryInstrumentField;
import net.jctp.TraderApi;

import org.apache.log4j.Logger;

import com.xun360.guojin.dataexchange.exception.CollectDataException;
import com.xun360.guojin.dataexchange.factory.TraderApiEntryFactory;
import com.xun360.guojin.dataexchange.model.AccountDetail;
import com.xun360.guojin.dataexchange.model.InstrumentDetialForInvestor;

public class InstrumentCollector implements Runnable,DataCollector{
	
	private static Logger log = Logger.getLogger(InstrumentCollector.class);
	
	/**行情接口 用户帐号**/
	private final static String MARKET_USER_ID;
	
	/**行情接口 用户密码**/
	private final static String MARKET_USER_PASSWORD;
	
	static{
		Properties configProps = new Properties();
		try {
			configProps.load(InstrumentCollector.class.getClassLoader().getResourceAsStream("config.properties"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		MARKET_USER_ID = configProps.getProperty("ctp.userId");
		MARKET_USER_PASSWORD = configProps.getProperty("ctp.password");
	}

	public void collect() throws CollectDataException  {
		/**交易接口**/
		TraderApi traderApi = null;
		try {
			traderApi = TraderApiEntryFactory.getFromCached(new AccountDetail(MARKET_USER_ID, MARKET_USER_PASSWORD,0));
			// 获取期货合约列表数据
			log.info("获取期货合约列表");
			CThostFtdcQryInstrumentField cThostFtdcQryInstrumentField = new CThostFtdcQryInstrumentField();
			CThostFtdcInstrumentField[] cThostFtdcInstrumentFields = traderApi
					.SyncAllReqQryInstrument(cThostFtdcQryInstrumentField);
			CollectedDataCache.instrumentsMap.clear();
			for(CThostFtdcInstrumentField cThostFtdcInstrumentField : cThostFtdcInstrumentFields){
				CollectedDataCache.instrumentsMap.put(cThostFtdcInstrumentField.InstrumentID, new InstrumentDetialForInvestor(cThostFtdcInstrumentField));
			}
			log.info("获取期货合约列表成功……共" + cThostFtdcInstrumentFields.length
					+ "条");
		} catch (Exception e) {
			log.error(e.getMessage());
			throw new CollectDataException(e.getMessage());
		} finally {
//			TraderApiEntryFactory.eliminateTraderApi(traderApi,MARKET_USER_ID);
		}
	}
	
	@Override
	public void run(){
		try {
			collect();
		} catch (CollectDataException e) {
			throw new IllegalStateException(e.getMessage());
		}
	}
}
