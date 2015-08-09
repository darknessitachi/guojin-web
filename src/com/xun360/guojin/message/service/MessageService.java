package com.xun360.guojin.message.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.xun360.guojin.message.bean.MessageResponse;
import com.xun360.guojin.message.dao.MessageDao;

@Service
public class MessageService {

	@Autowired
	private MessageDao messageDao;
	
	public MessageResponse getMessage(int clientID,int type){
		
		return messageDao.getMessage(clientID, type);
	}
}
