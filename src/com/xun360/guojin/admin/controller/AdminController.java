package com.xun360.guojin.admin.controller;

import java.text.SimpleDateFormat;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xun360.guojin.admin.service.AdminService;
import com.xun360.guojin.util.DESUtils;
import com.xun360.guojin.util.MD5Util;
/**
 * @param request.adminName 管理员帐号:admin
 * @param request.adminPassword 管理员密码:guojin123
 * 详细见util.AdminInterceptor
 * 
 * @return “” 验证帐号失败
 */
@Controller
@RequestMapping(value="admin")
public class AdminController {

	private ObjectMapper mapper=new ObjectMapper();
	@Autowired
	private AdminService adminService;
	
	private DESUtils desUtils = new DESUtils();
	
	/**
	 * 增加分组
	 * @param request.name 分组名称
	 * @param request.max 分组允许最大投顾数量
	 * 
	 * @return “1”
	 */
	@RequestMapping(value="/group/add")
	@ResponseBody
	public String addGroup(HttpServletRequest request,HttpServletResponse response){
		String name=request.getParameter("name");
		int maxUser=Integer.parseInt(request.getParameter("max"));
		
		return adminService.addGroup(name, maxUser);
	}
	
	/**
	 * 修改分组参数
	 * @param request.name 分组名称
	 * @param request.max 分组允许最大投顾数量
	 * @param request.id 分组id
	 * 
	 * @return “1”
	 */
	@RequestMapping(value="/group/change")
	@ResponseBody
	public String changeGroup(HttpServletRequest request,HttpServletResponse response){
		int id=Integer.parseInt(request.getParameter("id"));
		String name=request.getParameter("name");
		int maxUser=Integer.parseInt(request.getParameter("max"));
		
		return adminService.changeGroup(id, name, maxUser);
	}
	
	/**
	 * 删除分组
	 * @param request.id 分组id
	 * 
	 * @return “1”
	 */
	@RequestMapping(value="/group/delete")
	@ResponseBody
	public String deleteGroup(HttpServletRequest request,HttpServletResponse response){
		int id=Integer.parseInt(request.getParameter("id"));
		
		return adminService.deleteGroup(id);
	}
	
	/**
	 * 查询分组
	 * 
	 * @return 
	 * [
		  {
		    "id": 1,
		    "name": "测试组",
		    "max": 10,
		    "now": 2,
		    "createTime": "2014-09-22"
		  },
		  {
		    "id": 2,
		    "name": "管理组",
		    "max": 5,
		    "now": 1,
		    "createTime": "2014-09-22"
		  }
		]
	 */
	@RequestMapping(value="/group/get")
	@ResponseBody
	public String getGroup(HttpServletRequest request,HttpServletResponse response) throws JsonProcessingException{
		
		mapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd"));
		return mapper.writeValueAsString(adminService.getGroup());
	}
	//------------------------------manager--------------------------
	
	/**
	 * 增加投顾
	 * @param request.name 投顾帐号
	 * @param request.fullName 投顾名字
	 * @param request.password 密码
	 * @param request.groupID 分组ID
	 * @param request.permission 投顾权限 001:允许添加风控,010:允许强平,100:启用投顾(2进制)
	 * 
	 * @return 成功 “1”
	 * @return 失败 “0”
	 */
	@RequestMapping(value="/manager/add")
	@ResponseBody
	public String addManager(HttpServletRequest request,HttpServletResponse response){
		String name=request.getParameter("name");
		String fullName=request.getParameter("fullName");
		String password=MD5Util.generatePassword(desUtils.strDec(request.getParameter("password")));
		int groupID=Integer.parseInt(request.getParameter("groupID"));
		int permission=Integer.parseInt(request.getParameter("permission"));
		int autoConfirm=Integer.parseInt(request.getParameter("autoConfirm"));
		return adminService.addManager(name,fullName, password, groupID, permission,autoConfirm);
	}
	
	/**
	 * 修改投顾信息
	 * @param request.id 投顾id
	 * @param request.name 投顾帐号
	 * @param request.fullName 投顾名字
	 * @param request.password 密码
	 * @param request.groupID 分组ID
	 * @param request.permission 投顾权限 001:允许添加风控,010:允许强平,100:启用投顾(2进制)
	 * 
	 * @return 成功 “1”
	 * @return 失败 “0”
	 */
	@RequestMapping(value="/manager/change")
	@ResponseBody
	public String changeManager(HttpServletRequest request,HttpServletResponse response){
		int id=Integer.parseInt(request.getParameter("id"));
		String name=request.getParameter("name");
		String fullName=request.getParameter("fullName");
		String password = null;
		if(request.getParameter("password") != null && !"".equals(request.getParameter("password"))){
			password=MD5Util.generatePassword(desUtils.strDec(request.getParameter("password")));
		}
		int groupID=Integer.parseInt(request.getParameter("groupID"));
		int permission=Integer.parseInt(request.getParameter("permission"));
		int autoConfirm=Integer.parseInt(request.getParameter("autoConfirm"));
		return adminService.changeManager(id, name,fullName, password, groupID, permission,autoConfirm);
	}
	
	/**
	 * 删除投顾
	 * @param request.id 投顾id
	 * 
	 * @return 成功 “1”
	 */
	@RequestMapping(value="/manager/delete")
	@ResponseBody
	public String deleteManager(HttpServletRequest request,HttpServletResponse response){
		int id=Integer.parseInt(request.getParameter("id"));
		return adminService.deleteManager(id);
	}
	
	/**
	 * 获取投顾列表
	 * 
	 * @return 
	 * [
		  {
		    "id": 14,
		    "fullName": "姜维",
		    "name": "xc",
		    "groupID": 70,
		    "groupName": null,
		    "max": 0,
		    "now": 0,
		    "permission": 7,
		    "autoConfirm" : 1
		  },
		  {
		    "id": 15,
		    "fullName": "姜维",
		    "name": "admin",
		    "groupID": 2,
		    "groupName": "管理组",
		    "max": 5,
		    "now": 3,
		    "permission": 7,
		    "autoConfirm" : 1
		  }
		]
	 */
	@RequestMapping(value="/manager/get")
	@ResponseBody
	public String getManager(HttpServletRequest request,HttpServletResponse response) throws JsonProcessingException{
		String groupId = request.getParameter("groupId");
		return mapper.writeValueAsString(adminService.getManager(groupId));
	}
	
}
