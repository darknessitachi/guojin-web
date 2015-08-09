package com.xun360.guojin.configure.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xun360.guojin.configure.bean.ConfigureInfo;
import com.xun360.guojin.configure.bean.RiskComponent;
import com.xun360.guojin.configure.bean.indecator.AfterBadMoney;
import com.xun360.guojin.configure.bean.indecator.AllDay;
import com.xun360.guojin.configure.bean.indecator.Indecator;
import com.xun360.guojin.configure.bean.indecator.PositionsLimit;
import com.xun360.guojin.configure.bean.indecator.Reback;
import com.xun360.guojin.configure.bean.indecator.SpecialTime;
import com.xun360.guojin.configure.bean.indecator.Variety;
import com.xun360.guojin.util.Common;
import com.xun360.guojin.util.ErrorLog;
import com.xun360.guojin.util.SystemConfig;
import com.xun360.guojin.util.bean.ClientFundsData;

@Repository
public class ConfigureDao {

	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	public ConcurrentHashMap<Integer,List<ConfigureInfo>> getAllConfigure(){
		final ConcurrentHashMap<Integer,List<ConfigureInfo>> map=new ConcurrentHashMap<Integer,List<ConfigureInfo>>();
		String sql="SELECT *,investorID,configure.id AS configureID FROM configure,client WHERE client.id=clientID ORDER BY userID,clientID";
		jdbcTemplate.query(sql, new RowCallbackHandler() {
			ConfigureInfo info=new ConfigureInfo();
			List<ConfigureInfo> infoList=new ArrayList<ConfigureInfo>();
			@Override
			public void processRow(ResultSet rs) throws SQLException {
				//int len=response.size();
				//获取参数
				int userID=rs.getInt("userID");
				int clientID=rs.getInt("clientID");
				Indecator tmp=getIndecatorByString(clientID,
						rs.getInt("type"),
						rs.getString("criteria"),
						rs.getInt("configureID"));
				if(tmp==null){
					ErrorLog.log("ConfigureDao.getAll 解析configure出错",
							"-id:"+rs.getInt("configureID")+
							"-type:"+rs.getString("type")+
							"-criteria:"+rs.getString("criteria"));
					return ;
				}
				//新投顾
				if(map.get(userID)==null){
					infoList=new ArrayList<ConfigureInfo>();
					info=new ConfigureInfo();
					info.setClientID(clientID);
					info.setInvestorID(rs.getString("investorID"));
					info.setIndecator(new ArrayList<Indecator>());
					info.getIndecator().add(tmp);
					infoList.add(info);
					map.put(userID, infoList);
				}
				else {
					int len=map.get(userID).size();
					//新期货用户
					if(len==0||clientID!=map.get(userID).get(len-1).getClientID()){
						info=new ConfigureInfo();
						info.setClientID(clientID);
						info.setInvestorID(rs.getString("investorID"));
						info.setIndecator(new ArrayList<Indecator>());
						info.getIndecator().add(tmp);
						map.get(userID).add(info);
					}
					//新风控指标
					else {
						map.get(userID).get(len-1).getIndecator().add(tmp);
					}
				}
					
			}
		});
		
		return map;
	}
	
	private Indecator getIndecatorByString(int clientID,int type,String criteria,int id){
		Indecator indecator=null;
		ClientFundsData data=Common.clientData.get(clientID);
		if(data==null){
			return null;
		}
		
		try{
			switch(type){
			case 1:
				indecator=new AllDay(id,type,criteria,data.initFunds);
				break;
			case 2:
				indecator=new SpecialTime(id, type, criteria, data.initFunds);
				break;
			case 3:
				indecator=new PositionsLimit(id, type, criteria);
				break;
			case 4:
				indecator=new Variety(id, type, criteria);
				break;
			case 5:
				indecator=new Reback(id, type, criteria);
				break;
			case 6:
				indecator=new AfterBadMoney(id, type, criteria, data.afterFunds);
				break;
			default:
			}
		}
		catch(Exception e){
			
			return null;
		}
		return indecator;
	}
	

	
	public String changeConfigure(final int clientID,String types,String criterias){
		int uid=Common.getUIDByClientID(clientID);
		String type[]=types.split("\\|");
		String criteria[]=criterias.split("\\|");
		//删除数据库中原有的configure
		jdbcTemplate.update("DELETE FROM configure WHERE clientID=?",new Object[]{clientID});
		//输入为空
		if(types.equals("")){
			synchronized (Common.sync) {
				List<ConfigureInfo> infoList=Common.info.get(uid);
				if(infoList!=null){
//					Common.info.put(uid, new ArrayList<ConfigureInfo>());
					for(int i=0;i<infoList.size();i++){
						if(infoList.get(i).getClientID()==clientID){
							infoList.remove(i);
						}
					}
				}
			}
			return "1";
		}
		//将configure插入数据库
		for(int i=0;i<type.length;i++){
			Indecator indecator=getIndecatorByString(clientID, Integer.parseInt(type[i]), criteria[i], 0);
			if(indecator==null){
				ErrorLog.log("ConfigureDao.change 解析configure出错",
						"-type:"+type[i]+
						"-criteria:"+criteria[i]);
				return "0";
			}
			String sql="INSERT INTO configure VALUES(NULL,?,?,?)";
			jdbcTemplate.update(sql,new Object[]{clientID,type[i],criteria[i]});
		}
		//读取configureinfo
		String sql="SELECT *,investorID,configure.id AS configureID FROM configure,client WHERE client.id=clientID AND clientID=?";
		final ConfigureInfo conf=new ConfigureInfo();
		conf.setIndecator(new ArrayList<Indecator>());
		jdbcTemplate.query(sql, new Object[]{clientID},
				new RowCallbackHandler() {
					@Override
					public void processRow(ResultSet rs) throws SQLException {
						conf.setClientID(clientID);
						conf.setInvestorID(rs.getString("investorID"));
						Indecator tmp=getIndecatorByString(clientID,
								rs.getInt("type"),
								rs.getString("criteria"),
								rs.getInt("configureID"));
						if(tmp==null){
							ErrorLog.log("ConfigureDao.getAll 解析configure出错",
									"-id:"+rs.getInt("configureID")+
									"-type:"+rs.getString("type")+
									"-criteria:"+rs.getString("criteria"));
							return ;
						}
						conf.getIndecator().add(tmp);
					}
				});
		//将configureinfo写入内存
		synchronized (Common.sync) {
			List<ConfigureInfo> infoList=Common.info.get(uid);
			if(infoList==null){
				List<ConfigureInfo> newList=new ArrayList<ConfigureInfo>();
				newList.add(conf);
				Common.info.put(uid, newList);
			}
			else {
				for(int i=0;i<infoList.size();i++){
					if(infoList.get(i).getClientID()==clientID){
						infoList.remove(i);
						break;
					}
				}
				infoList.add(conf);
			}
		}
		return "1";
	}
	
	public String getConfigure(int clientID){
		String sql="SELECT * FROM configure WHERE clientID=?";
		final List<Integer> type=new ArrayList<Integer>();
		final List<String> list=new ArrayList<String>();
		jdbcTemplate.query(sql, new Object[]{clientID},
				new RowCallbackHandler() {
					@Override
					public void processRow(ResultSet rs) throws SQLException {
						type.add(rs.getInt("type"));
						String criteria=rs.getString("criteria");
						list.add(criteria);
					}
				});
		ObjectMapper mapper=new ObjectMapper();
		try {
			return mapper.writeValueAsString(new Object[]{type,list});
		} catch (JsonProcessingException e) {
			return "0";
		}
	}
	
	public Map<Integer,ClientFundsData> getClientData(){
		final Map<Integer,ClientFundsData> map=new HashMap<Integer,ClientFundsData>();
//		String sql="SELECT * FROM client ORDER BY id";
		String sql="SELECT t.* FROM client t join user u on t.userID=u.id where u.serverNum=" + SystemConfig.THIS_SERVER_NUM + " ORDER BY t.id";
		jdbcTemplate.query(sql, new RowCallbackHandler() {
			@Override
			public void processRow(ResultSet rs) throws SQLException {
				ClientFundsData data=new ClientFundsData();
				data.uid=rs.getInt("userID");
				data.clientID=rs.getInt("id");
				data.investorID=rs.getString("investorID");
				data.password=rs.getString("password");
				data.clientName=rs.getString("clientName");
				data.initFunds=rs.getDouble("initFunds");
				data.afterFunds=rs.getDouble("afterFunds");
				map.put(rs.getInt("id"), data);
			}
		});
		return map;
	}
}
