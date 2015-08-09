package com.xun360.guojin.admin.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.xun360.guojin.admin.bean.LoginUserInfo;
import com.xun360.guojin.admin.dao.UserDao;

@Service
public class UserService {

	@Autowired
	private UserDao userDao;
	
	public LoginUserInfo login(String name,String password){
		return userDao.login(name, password);
	}
	
	public String regist(String name,String password){
		
		return userDao.regist(name, password);
	}

	public boolean modifyPassword(String name, String oldPassword,
			String newPassword) {
		return userDao.modifyPassword(name,oldPassword,newPassword);
		
	}
}
