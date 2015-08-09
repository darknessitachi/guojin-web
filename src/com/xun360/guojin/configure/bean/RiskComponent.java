package com.xun360.guojin.configure.bean;

import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.xun360.guojin.configure.bean.indecator.Indecator;
import com.xun360.guojin.configure.bean.indecator.Variety;
import com.xun360.guojin.dataexchange.collector.CollectedDataCache;
import com.xun360.guojin.dataexchange.model.WrapAccountPosition;
import com.xun360.guojin.dataexchange.model.WrapAccountPosition.WrapAccountPositionSnapShot;
import com.xun360.guojin.dataexchange.model.WrapAccountTradingMoney;
import com.xun360.guojin.dataexchange.model.WrapAccountTradingMoney.WrapAccountTradingMoneySnapShot;
import com.xun360.guojin.util.Common;
import com.xun360.guojin.util.ErrorLog;

public class RiskComponent {

	/**
	 * 计算所有账户的风险
	 */
	public static void computeAllInvestorRisk(){
//		System.out.println("风控开始…………");
		Set<Integer> set=Common.info.keySet();
		for(Integer uid : set){
			List<ConfigureInfo> infoList=Common.info.get(uid);
			//极低概率情况
			if(infoList==null){
				ErrorLog.log("获取风控配置为空","帐号ID:"+uid);
				continue;
			}
			//遍历单个用户的风控
			for(int i=0;i<infoList.size();i++){
				synchronized (Common.sync) {
					try{
						WrapAccountTradingMoney accountMoneyData=CollectedDataCache.accountMoneyDataMap.get(infoList.get(i).getInvestorID());
						List<WrapAccountPosition> positionList=CollectedDataCache.accountPositionDataMap.get(infoList.get(i).getInvestorID());
						if(accountMoneyData==null){
							ErrorLog.log("缺少用户信息", "用户ID:"+infoList.get(i).getInvestorID());
							continue;
						}
						WrapAccountTradingMoneySnapShot entry=accountMoneyData.getSnapShot();
						//if(Common.info.get(i).getInvestorID().equals("00000023"))
//							System.out.println("帐号："+infoList.get(i).getInvestorID()+"\n动态权益："+entry.DynamicRight);
						ConfigureInfo conf=infoList.get(i);
						//遍历单个期货帐号的风控
						for(int j=0;j<conf.getIndecator().size();j++){
							Indecator indecator=conf.getIndecator().get(j);
							/*
							 * 1:资金全天候监控
							 * 2:资金量分时段监控
							 * 3:时间段开仓限制监控
							 * 4:品种资金量监控
							 * 5:资金回撤监控
							 * 6:劣后资金量监控
							 */
							switch(indecator.getType()){
							case 1:
							case 2:
								indecator.setValue(entry.DynamicRight, 0);
								break;
							case 3:
								boolean has = false;
								if(positionList != null){
									for(WrapAccountPosition accountPosition : positionList){
										WrapAccountPositionSnapShot positionSnapShot = accountPosition.getSnapShot();
										if(positionSnapShot.Position > 0){
											has = true;
											break;
										}
									}
								}
								indecator.setHasPosi(has);
								break;
							case 4:
								double now=0;
								String varName=((Variety)indecator).getVariety();
								for(int k=0;k<positionList.size();k++){
									if(varName.equals(positionList.get(k).getSnapShot().ProductID) 
											|| varName.equals(positionList.get(k).getSnapShot().InstrumentID)){
										now+=positionList.get(k).getSnapShot().UseMargin;
									}
								}
								indecator.setValue(now, entry.PreBalance);
								break;
							case 5:
								indecator.setValue(entry.TdHigestDynamicRight-entry.DynamicRight,
										entry.TdHigestDynamicRight);
								break;
							case 6:
								indecator.setValue(entry.DynamicRight+
										Common.clientData.get(conf.getClientID()).afterFunds-
										Common.clientData.get(conf.getClientID()).initFunds, 0);
								break;
							}
						}
						conf.riskControl();
					}
					catch(Exception e){
						e.printStackTrace();
						try {
							ErrorLog.log("风控执行错误",e,
									Common.mapper.writeValueAsString(CollectedDataCache.accountMoneyDataMap.get(infoList.get(i).getInvestorID()))+"\n"+
									Common.mapper.writeValueAsString(CollectedDataCache.accountPositionDataMap.get(infoList.get(i).getInvestorID()))+"\n"+
									infoList.get(i).getInvestorID()
									);
						} catch (JsonProcessingException e1) {
							
						}
					}
					
				}//synchronized (Common.sync)
			}//for(int i=0;i<Common.info.size();i++)
		}
//		System.out.println("风控结束…………");
	}
}
