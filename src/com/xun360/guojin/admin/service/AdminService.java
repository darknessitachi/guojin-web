package com.xun360.guojin.admin.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.xun360.guojin.admin.bean.GroupData;
import com.xun360.guojin.admin.bean.ManagerData;
import com.xun360.guojin.admin.dao.AdminDao;

@Service
public class AdminService {

	@Autowired
	private AdminDao adminDao;
	
	public String addGroup(String name,int maxUser){
		
		return adminDao.addGroup(name, maxUser);
	}
	
	public String changeGroup(int id,String name,int maxUser){
		
		return adminDao.changeGroup(id,name, maxUser);
	}
	
	public String deleteGroup(int id){
		
		return adminDao.deleteGroup(id);
	}
	
	public List<GroupData> getGroup(){
		
		return adminDao.getGroup();
	}
	
	public String addManager(String name,String fullName,String password,int groupID,int permission, int autoConfirm){
		
		return adminDao.addManager(name,fullName, password, groupID, permission, autoConfirm);
	}
	
	public String changeManager(int id,String name,String fullName,String password,int groupID,int permission, int autoConfirm){
		
		return adminDao.changeManager(id, name,fullName, password, groupID, permission,autoConfirm);
	}

	public String deleteManager(int id){
	
		return adminDao.deleteManager(id);
	}

	public List<ManagerData> getManager(String groupId){
		return adminDao.getManager(groupId);
	}
}
