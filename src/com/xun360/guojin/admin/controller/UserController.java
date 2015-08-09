package com.xun360.guojin.admin.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xun360.guojin.admin.service.UserService;
import com.xun360.guojin.util.Common;
import com.xun360.guojin.util.DESUtils;
import com.xun360.guojin.util.MD5Util;

@Controller
@RequestMapping(value="user")
public class UserController {

	@Autowired
	private UserService userService;
	private ObjectMapper mapper = new ObjectMapper();
	
	private DESUtils desUtils = new DESUtils();
	/**
	 * 用户登录
	 * @param request.name	用户名
	 * @param response.password 密码
	 * 
	 * @return 成功:433D06983C78522EF001D9591F1CD439
	 * @return 失败:0
	 * @throws JsonProcessingException 
	 */
	@RequestMapping(value="/login")
	@ResponseBody
	public String login(HttpServletRequest request,HttpServletResponse response) throws JsonProcessingException{
		String name=request.getParameter("name");
		String desPassword  = request.getParameter("password");
		String password=MD5Util.generatePassword(desUtils.strDec(desPassword));
		return mapper.writeValueAsString(userService.login(name, password));
	}
	/**
	 * 注册账号
	 * @param request.name	用户名
	 * @param response.password 密码
	 * 
	 * @return 成功:1	
	 * @return 失败:0
	 */
	@RequestMapping(value="/regist")
	@ResponseBody
	public String regist(HttpServletRequest request,HttpServletResponse response){
		String name=request.getParameter("name");
		String password=MD5Util.generatePassword(request.getParameter("password"));
		return userService.regist(name, desUtils.strDec(password));
	}
	/**
	 * 
	 * @param request.token 账号验证令牌
	 * 
	 * @return 成功：
	 * [
		  [
		    23,//clientID
		    24,
		    27,
		    28,
		    31
		  ],
		  [
		    2,//stat,0:正常,1:警告,2:平仓
		    2,
		    0,
		    0,
		    0
		  ]
		]
	 * @return 失败或者没有状态变化： “”  //空
	 */
	@RequestMapping(value="/stat")
	@ResponseBody
	public String getStat(HttpServletRequest request,HttpServletResponse response) throws JsonProcessingException{
		if((int)request.getAttribute("uid") <= 0){
			return "0";
		}
		String returnStr = Common.checkMessage(request);
		if("".equals(returnStr)){
			return "[]";
		}
		return returnStr;
	}
	
	/**
	 * 修改密码
	 * @param request
	 * @param response
	 * @return
	 * 1：成功 2：失败
	 * @throws JsonProcessingException
	 */
	@RequestMapping(value="/modifyPassword")
	@ResponseBody
	public String modifyPassword(HttpServletRequest request,HttpServletResponse response) throws JsonProcessingException{
		String name = request.getParameter("loginName");
		String oldPassword = MD5Util.generatePassword(desUtils.strDec(request.getParameter("oldPassword")));
		String newPassword = MD5Util.generatePassword(desUtils.strDec(request.getParameter("newPassword")));
		if(userService.modifyPassword(name,oldPassword,newPassword)){
			return "1";//修改成功
		}
		return "2";//修改失败
	}
}
