package com.xun360.guojin.configure.bean;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;

import com.xun360.guojin.configure.service.ConfigureService;
import com.xun360.guojin.dataexchange.conductor.CollectJobConductor;
import com.xun360.guojin.dataexchange.exception.CollectDataException;
import com.xun360.guojin.util.Common;
import com.xun360.guojin.util.ErrorLog;

@Controller
public class MainConfigure{
	
	private static final Logger log = Logger.getLogger(MainConfigure.class);
	
	@Autowired
	private JdbcTemplate jdbcTemplate;
	@Autowired
	private ConfigureService configureService;
	
	
	public void initConfigure() throws InterruptedException{
		System.out.println("init");
		CollectJob job=new CollectJob();
		job.start();
	}
	
	
	private void readInfo(){
		Common.info=configureService.getAllConfigure();
	}
	
	private void initClientData(){
		Common.clientData=configureService.getClientData();
	}
	
	private class CollectJob extends Thread{
		@Override
		public void run() {
			boolean instrumentsInit = true;
			//初始化参数
			log.info("初始化系统开始……");
			Common.jdbcTemplate=jdbcTemplate;
			Thread thread=new ConfigureThread();
			Common.sync=thread;
			log.info("初始化dataexchenage……开始");
			try {
				//初始化帐号
				//启动获取期货数据的接口
				CollectJobConductor.initBaseData();
			} catch (CollectDataException e) {
				ErrorLog.log("CollectJobConductor", e, "");
				log.error("初始化dataexchenage异常；原因" + e.getMessage());
				instrumentsInit = false;
			}
			log.info("初始化dataexchenage……完成");
			//初始化用户资金信息
			log.info("初始化用户资金信息");
			initClientData();
			//初始化风控配置信息
			log.info("初始化风控配置信息");
			readInfo();
			log.info("风控配置初始化完成");
			//启动 账户 资金 持仓 行情的收集，并触发风控
			if(instrumentsInit){
				log.info("启动账户资金、持仓、行情的收集，并触发风控");
				CollectJobConductor.startMarketDataCollectJob();
				CollectJobConductor.startTradingCollectJob();
				CollectJobConductor.startRiskControl();
			}else{
				log.warn("因dataexchenage初始化异常，默认不启动账户资金、持仓、行情的收集，并触发风控");
			}
		}
	}
}

