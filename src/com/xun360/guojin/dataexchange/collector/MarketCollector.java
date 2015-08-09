package com.xun360.guojin.dataexchange.collector;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

import net.jctp.MdApi;

import org.apache.log4j.Logger;

import com.xun360.guojin.dataexchange.cfg.ExchangeConfig;
import com.xun360.guojin.dataexchange.listener.MarketApiImplListener;

public class MarketCollector implements Runnable{
	
	private final static Integer SubInstrumentsSize = 200;
	
	private static Logger log = Logger.getLogger(MarketCollector.class);
	
	/**行情获取情况统计**/
	private final ConcurrentMap<String, AtomicLong> statMap = new ConcurrentHashMap<String, AtomicLong>(); 

	/**行情接口**/
	private List<MdApi> mdApis = new ArrayList<MdApi>();
//	private MdApi mdApi = new MdApi("", true, false);//使用udp的方式
	
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

	public void collect() {
		try {
			// 根据合约列表数据 获取各个合约的行情
			log.info("根据合约列表数据 获取各个合约的行情……");
			List<String> instrumentIds = new ArrayList<>(CollectedDataCache.instrumentsMap.keySet());
			Collections.sort(instrumentIds);
			double subSribeTimes = (instrumentIds.size()/SubInstrumentsSize) + ((instrumentIds.size()%SubInstrumentsSize == 0 ? 0 : 1));
			log.info("用户" + MARKET_USER_ID + "通过[" + ExchangeConfig.MARKET_FRONT_URL + "][" + ExchangeConfig.BROKER_ID + "]链接……行情接口");
			for(int i = 0 ; i < subSribeTimes ; i++){
				MdApi mdApi = new MdApi("", true, false);//使用udp的方式
				mdApi.setListener(new MarketApiImplListener(statMap));
				mdApi.Connect(ExchangeConfig.MARKET_FRONT_URL, ExchangeConfig.BROKER_ID, MARKET_USER_ID, MARKET_USER_PASSWORD);
				mdApis.add(mdApi);
				boolean subscribed = false;
				while(true){
		            try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
					}
		            if (mdApi.isConnected() && mdApi.isLogin() && !subscribed ){
		            	log.info("用户" +MARKET_USER_ID+ "登录 行情接口……成功");
		                subscribed = true;
		                int startIndex = SubInstrumentsSize*i;
		                int endIndex = Math.min(SubInstrumentsSize*(i+1), instrumentIds.size());
		                List<String> subsribedInsturments = instrumentIds.subList(startIndex, endIndex);
		                mdApi.SubscribeMarketData(subsribedInsturments.toArray(new String[0]));
		                log.info("行情订阅了合约号:" + subsribedInsturments);
		                break;
		            }
		        }
			}
			while(!Thread.currentThread().isInterrupted()){
				try {
					Thread.sleep(1800*1000);
					for(MdApi mdApi : mdApis){
						if(!mdApi.isConnected() || !mdApi.isLogin()){
							//mdApi.setListener(new MarketApiImplListener(statMap));
							mdApi.Connect(ExchangeConfig.MARKET_FRONT_URL, ExchangeConfig.BROKER_ID, MARKET_USER_ID, MARKET_USER_PASSWORD);
						}
					}
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
				stat();
			}
		} catch (Exception e) {
			e.printStackTrace();
			log.error(e.getMessage());
		} finally{
			for(MdApi mdApi : mdApis){
				if (mdApi != null && mdApi.isConnected() ){
	                try {
	                	mdApi.Close();
	                } catch (Exception ex) {}
	            }
			}
		}
	}

	public void stat(){
		StringBuilder builder = new StringBuilder();
		builder.append("\n行情获取情况为：\n----------------------------------------------------\n");
		for(String instrumentId : statMap.keySet()){
			builder.append("合约号：").append(instrumentId).append("    调用行情次数为").append(statMap.get(instrumentId)).append("次\n");
		}
		builder.append("----------------------------------------------------\n");
		log.info(builder.toString());
	}
	
	@Override
	public void run() {
		collect();
	}
}
