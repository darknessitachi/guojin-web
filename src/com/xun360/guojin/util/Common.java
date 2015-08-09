package com.xun360.guojin.util;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xun360.guojin.configure.bean.ConfigureInfo;
import com.xun360.guojin.util.bean.ClientFundsData;
import com.xun360.guojin.util.bean.WarningStat;

public class Common {

	public static JdbcTemplate jdbcTemplate;

	public static Thread sync;
	
	public static ObjectMapper mapper=new ObjectMapper();
	
	/*
	 * 用户风控配置
	 * key:uid
	 */
	public static ConcurrentHashMap<Integer,List<ConfigureInfo>> info = new ConcurrentHashMap<Integer, List<ConfigureInfo>>();
	/*
	 * 用户基本资金信息
	 * key:clientID
	 */
	public static Map<Integer,ClientFundsData> clientData=new HashMap<Integer, ClientFundsData>();
	/*
	 * 当前用户状态，正常，警告或平仓
	 * key:uid
	 */
	public static Map<Integer,WarningStat> stat=new HashMap<Integer,WarningStat>();
	/*
	 * 是否正在平仓状态
	 * key:investorID
	 */
	public static Map<String,Boolean> closeStat=new HashMap<String,Boolean>();
	
	public static String getInvestorIDByClientID(int clientID){
		String sql="SELECT * FROM client WHERE id=?";
		final Set<String> investorID = new HashSet<String>();
		jdbcTemplate.query(sql, new Object[]{clientID},
				new RowCallbackHandler() {
					@Override
					public void processRow(ResultSet rs) throws SQLException {
						investorID.add(rs.getString("investorID"));
					}
				});
		if(investorID.iterator().hasNext()){
			return investorID.iterator().next();
		}else{
			return null;
		}
	}
	
	public static int getUIDByClientID(int clientID){
		return clientData.get(clientID).uid;
	}
	
	
	public static String checkMessage(HttpServletRequest request) throws JsonProcessingException{
		String token=request.getParameter("token");
		int uid=(int)request.getAttribute("uid");
		if(uid==0){
			return "";
		}
		List<Integer> clientID=new ArrayList<Integer>();
		List<Integer> clientStat=new ArrayList<Integer>();
		List<ConfigureInfo> infoList=info.get(uid);
		//获取用户的warningStat
		WarningStat warningStat=stat.get(uid);
		if(warningStat==null){
			warningStat = new WarningStat();
			stat.put(uid, warningStat);
		}
		//已经登录的用户，只查看变化量
		if(token.equals(warningStat.token)){
			for(Integer clientid : warningStat.statMap.keySet()){
				boolean found = false;
				for(int i=0;infoList != null && i<infoList.size();i++){
					int tmpID=infoList.get(i).getClientID();
					int tmpStat=infoList.get(i).getStat();
					if(clientid == tmpID){
						if(tmpStat != warningStat.statMap.get(tmpID)){
							clientID.add(tmpID);
							clientStat.add(tmpStat);
							warningStat.statMap.put(tmpID, tmpStat);
						}
						found = true;
						break;
					}
				}
				if(!found && warningStat.statMap.get(clientid) != -1){
					clientID.add(clientid);
					clientStat.add(-1);
					warningStat.statMap.put(clientid, -1);
				}
			}
//			for(int i=0;infoList != null && i<infoList.size();i++){
//				int tmpID=infoList.get(i).getClientID();
//				int tmpStat=infoList.get(i).getStat();
//				if(warningStat.statMap.get(tmpID)==null||warningStat.statMap.get(tmpID)!=tmpStat){
//					warningStat.statMap.put(tmpID, tmpStat);
//					clientID.add(tmpID);
//					clientStat.add(tmpStat);
//				}
//			}
		}
		//未登录用户，查看全部状态
		else{
			warningStat.statMap=new HashMap<Integer,Integer>();
			for(Integer clientid : clientData.keySet()){
				if(clientData.get(clientid).uid == uid){
					boolean found = false;
					for(int i=0;infoList != null && i<infoList.size();i++){
						int tmpID=infoList.get(i).getClientID();
						int tmpStat=infoList.get(i).getStat();
						if(clientid == tmpID){
							clientID.add(tmpID);
							clientStat.add(tmpStat);
							warningStat.statMap.put(tmpID, tmpStat);
							found = true;
							break;
						}
					}
					if(!found){
						warningStat.statMap.put(clientid, -1);
					}
				}
			}
//			for(int i=0;infoList != null && i<infoList.size();i++){
//				int tmpID=infoList.get(i).getClientID();
//				int tmpStat=infoList.get(i).getStat();
//				clientID.add(tmpID);
//				clientStat.add(tmpStat);
//				warningStat.statMap.put(tmpID, tmpStat);
//			}
			warningStat.token=token;
		}
		if(clientID.size()==0){
			return "";
		}
		else{
			return mapper.writeValueAsString(new Object[]{clientID,clientStat});
		}
	}
	
	/**
	 * 根据 uid投顾id 和 client管理账户id 获取此账户的状态
	 * @param uid
	 * @param ClientId
	 * @return 0-正常 1-警告 2-平仓 ""-无风控设置
	 */
	public static String getCurrentStatByUserIdAndClientID(int uid,int ClientId){
		List<ConfigureInfo> infoList = info.get(uid);
		ConfigureInfo currentUserConfig = null;
		if(infoList == null){
			return "";
		}
		for(ConfigureInfo configureInfo : infoList){
			if(configureInfo.getClientID() == ClientId){
				currentUserConfig = configureInfo;
				break;
			}
		}
		if(currentUserConfig != null 
				&& currentUserConfig.getIndecator() != null 
				&& currentUserConfig.getIndecator().size() > 0){
			return String.valueOf(currentUserConfig.getStat());
		}
		return "";
	}
	
	public static void delay(long k){
		try {
			Thread.sleep(k);
		} catch (InterruptedException e) {
		}
	}
}
