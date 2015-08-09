package com.xun360.guojin.client.service;

import net.jctp.JctpException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.xun360.guojin.client.bean.ClientBasic;
import com.xun360.guojin.client.bean.ClientInfoData;
import com.xun360.guojin.client.bean.ClientListResponse;
import com.xun360.guojin.client.bean.FundsResponse;
import com.xun360.guojin.client.dao.ClientDao;
import com.xun360.guojin.util.bean.ClientFundsData;

@Service
public class ClientService {

	@Autowired
	private ClientDao clientDao;
	
	public ClientFundsData addClient(int uid,
							String name,
							String password,
							String clientName,
							int initFunds,
							int afterFunds) throws JctpException{
		
		return clientDao.addClient(uid,
									name,
									password,
									clientName,
									initFunds,
									afterFunds);
	}
	
	public void changeClient(int clientID,
			String name,
			String password,
			String clientName,
			int initFunds,
			int afterFunds){
		clientDao.changeClient(clientID,
					name,
					password,
					clientName,
					initFunds,
					afterFunds);
	}
	
	public ClientListResponse getClientList(int uid)throws Exception{
		
		return clientDao.getClientList(uid);
	}
	
	public FundsResponse getFunds(int clientID)throws Exception{
		
		return clientDao.getFunds(clientID);
	}
	
	public ClientBasic getBasic(int clientID){
		
		return clientDao.getBasic(clientID);
	}
	
	public void deleteClient(int clientID){
		
		clientDao.deleteClient(clientID);
	}

	public ClientInfoData getClientByInvestorID(String name) {
		return clientDao.getClientByInvestorID(name);
	}
}
