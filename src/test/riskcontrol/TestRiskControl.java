package test.riskcontrol;

import net.jctp.JctpConstants;
import net.jctp.JctpException;
import net.jctp.TraderApi;

import org.junit.Before;
import org.junit.Test;

import com.xun360.guojin.dataexchange.cfg.ExchangeConfig;
import com.xun360.guojin.dataexchange.listener.TraderApiImplListener;



public class TestRiskControl {

	@Before
	public void init(){
		
	}
	
	@Test
	public void testRisk(){
		
	}
	
	public static void main(String[] args) throws JctpException {
		for(int i = 1 ; i < 200 ; i++){
			TraderApi traderApi = new TraderApi();
			traderApi.setListener(new TraderApiImplListener());
			traderApi.setAutoSleepReqQry(true);
			traderApi.SubscribePrivateTopic(JctpConstants.THOST_TERT_QUICK);
			traderApi.SubscribePublicTopic(JctpConstants.THOST_TERT_QUICK);
			traderApi.SyncConnect(ExchangeConfig.TRADER_FRONT_URL);
			System.out.println(i + " " + traderApi.isConnected());
			try {
				Thread.sleep(300);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
