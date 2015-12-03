package com.xun360.guojin.dataexchange.cfg;

import java.io.IOException;
import java.util.Properties;

public class ExchangeConfig {

	public final static String TRADER_FRONT_URL;

	public final static String MARKET_FRONT_URL;

	public final static String BROKER_ID;
	
	public final static String USER_PRODUCT_INFO;
	
	public final static String AUTH_CODE;
	
	public final static Boolean RISK_CONTROL;
	
	public final static int DATA_UPDATE_DURATION;

	static {
		Properties configProps = new Properties();
		try {
//			configProps.load(ExchangeConfig.class
//					.getResourceAsStream("config.properties"));
			configProps.load(ExchangeConfig.class.getClassLoader().getResourceAsStream("config.properties"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		MARKET_FRONT_URL = configProps.getProperty("ctp.mdFrontUrl");
		TRADER_FRONT_URL = configProps.getProperty("ctp.traderFrontUrl");
		BROKER_ID = configProps.getProperty("ctp.brokerId");
		USER_PRODUCT_INFO = configProps.getProperty("ctp.userProductInfo");
		AUTH_CODE = configProps.getProperty("ctp.authCode");
		RISK_CONTROL = Boolean.valueOf(configProps.getProperty("ctp.risk.control"));
		DATA_UPDATE_DURATION = Integer.valueOf(configProps.getProperty("data.update.duration"));
	}
}

