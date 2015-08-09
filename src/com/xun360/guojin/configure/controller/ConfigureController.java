package com.xun360.guojin.configure.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xun360.guojin.configure.bean.RiskComponent;
import com.xun360.guojin.configure.service.ConfigureService;

@Controller
@RequestMapping(value="configure")
public class ConfigureController {

	private ObjectMapper mapper=new ObjectMapper();
	@Autowired
	private ConfigureService configureService;
	/**
	 * 获取客户期货账号持仓信息
	 * @param request.token 账号验证令牌
	 * @param request.cliendID 客户期货账号ID
	 * 
	 * @return 成功:1
				
	 * @return 失败:0
	 */
	@RequestMapping(value="/change")
	@ResponseBody
	public String changeConfigure(HttpServletRequest request,HttpServletResponse response){
		
		int clientID=(int)request.getAttribute("clientID");
		if(clientID <= 0){
			return "0";
		}
		String types = request.getParameter("type");
		types = (types == null ? "" : types);
		String criterias = request.getParameter("criteria");
		criterias = (criterias == null ? "" : criterias);
		String returnStr = configureService.changeConfigure(clientID, types, criterias);
		//风控
		RiskComponent.computeAllInvestorRisk();
		return returnStr;
	}
	
	/**
	 * 获取客户期货账号持仓信息
	 * @param request.token 账号验证令牌
	 * @param request.cliendID 客户期货账号ID
	 * 
	 * @return 成功:
	 * [
		  [
		    2,
		    1,
		    1
		  ],
		  [
		    "1*32.5*2*3135*2*13.5",
		    "1*32.5*2*3135*2*13.5",
		    "1*32.5*2*3135*2*13.5"
		  ]
		]
				
	 * @return 失败:
	 * [
		  [
		    
		  ],
		  [
		    
		  ]
		]
	 */
	@RequestMapping(value="/get")
	@ResponseBody
	public String getConfigure(HttpServletRequest request,HttpServletResponse response){
		int clientID=(int)request.getAttribute("clientID");
		
		return configureService.getConfigure(clientID);
	}
}
