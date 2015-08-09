package com.xun360.guojin.util;

import java.io.IOException;
import java.util.Properties;

import com.xun360.guojin.dataexchange.cfg.ExchangeConfig;

public class SystemConfig {
	
	public static final Integer TOTAL_NUM;
	
	public static final String SERVER_1_IP;
	public static final String SERVER_2_IP;
	public static final String SERVER_3_IP;
	public static final String SERVER_4_IP;
	public static final String SERVER_5_IP;
	
	public static final Integer THIS_SERVER_NUM;
	
	static {
		Properties configProps = new Properties();
		try {
			configProps.load(ExchangeConfig.class.getClassLoader().getResourceAsStream("config.properties"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		TOTAL_NUM = Integer.valueOf(configProps.getProperty("server.totalNum"));
		THIS_SERVER_NUM = Integer.valueOf(configProps.getProperty("server.thisServerNum"));
		SERVER_1_IP = configProps.getProperty("server.1.ip");
		SERVER_2_IP = configProps.getProperty("server.2.ip");
		SERVER_3_IP = configProps.getProperty("server.3.ip");
		SERVER_4_IP = configProps.getProperty("server.4.ip");
		SERVER_5_IP = configProps.getProperty("server.5.ip");
		
	}
}
