package com.xun360.guojin.configure.service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.xun360.guojin.configure.bean.ConfigureInfo;
import com.xun360.guojin.configure.dao.ConfigureDao;
import com.xun360.guojin.util.bean.ClientFundsData;

@Service
public class ConfigureService {

	@Autowired
	private ConfigureDao configureDao;
	
	public ConcurrentHashMap<Integer,List<ConfigureInfo>> getAllConfigure(){
		
		return configureDao.getAllConfigure();
	}
	
	public String changeConfigure(int clientID,String types,String criterias){
		
		return configureDao.changeConfigure(clientID, types, criterias);
	}
	
	public String getConfigure(int clientID){
		
		return configureDao.getConfigure(clientID);
	}
	
	public Map<Integer,ClientFundsData> getClientData(){
		
		return configureDao.getClientData();
	}
}
