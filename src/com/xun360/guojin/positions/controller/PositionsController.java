package com.xun360.guojin.positions.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xun360.guojin.dataexchange.conductor.CollectJobConductor;
import com.xun360.guojin.positions.bean.PositionsListResponse;
import com.xun360.guojin.positions.bean.PositionsSummary;
import com.xun360.guojin.positions.service.PositionsService;

@Controller
@RequestMapping(value="positions")
public class PositionsController {

	private ObjectMapper mapper=new ObjectMapper();
	@Autowired
	private PositionsService positionsService;
	
	/**
	 * 获取客户期货账号持仓信息
	 * @param request.token 账号验证令牌
	 * @param request.cliendID 客户期货账号ID
	 * 
	 * @return 成功:
	 * {
		  "list": [
		    {
		      "contractID": "IF1410",//合约ID
		      "contractName": "IF",//合约名
		      "contractType": 50,//合约类型,买:1,卖:2
		      "totalPositions": 1,//总持仓
		      "nowPositions": 1,//今仓
		      "lastPositions": 0,//昨仓
		      "usablePositions": 1,//可平量
		      "avePrice": 2407.8,//持仓均价
		      "profit": 539.9999999999181,//持仓盈亏
		      "deposit": 72288.0//占用保证金
		    },
		    {
		      "contractID": "IF1503",
		      "contractName": "IF",
		      "contractType": 50,
		      "totalPositions": 11,
		      "nowPositions": 11,
		      "lastPositions": 0,
		      "usablePositions": 11,
		      "avePrice": 2443.3272727272724,
		      "profit": 1560.0000000015825,
		      "deposit": 806454.0000000001
		    }
		  ]
		}
	 * @return 失败:0
	 */
	@RequestMapping(value="/detail")
	@ResponseBody
	public String getPositionsList(HttpServletRequest request,HttpServletResponse response) throws Exception{
		int clientID=(int)request.getAttribute("clientID");
		PositionsListResponse res=positionsService.getPositionsList(clientID);
		
		return mapper.writeValueAsString(res);
	}
	
	@RequestMapping(value="/refresh")
	@ResponseBody
	public String refreshPositionsList(HttpServletRequest request,HttpServletResponse response) throws Exception{
		if(request.getAttribute("investorID") != null){
			CollectJobConductor.refreshAccountPositionByAccount(String.valueOf(request.getAttribute("investorID")));
		}
		int clientID=(int)request.getAttribute("clientID");
		PositionsListResponse res=positionsService.getPositionsList(clientID);
		return mapper.writeValueAsString(res);
	}
	
	/**
	 * 获取客户期货账号持仓信息
	 * @param request.token 账号验证令牌
	 * 
	 * @return 成功:
				[
				  {
				    "clientID": 23,
				    "investorID": "00000023",  //账户名
				    "number": 10,              //持仓数
				    "money": 6.09266055E7      //持仓金额
				  },
				  {
				    "clientID": 24,
				    "investorID": "00000002",
				    "number": 0,
				    "money": 0.0
				  }
				]
	 * @return 失败:0
	 */
	@RequestMapping(value="/summary")
	@ResponseBody
	public String getPositionsSummary(HttpServletRequest request,HttpServletResponse response) throws Exception{
		
		int uid=(int)request.getAttribute("uid");
		List<PositionsSummary> list=positionsService.getSummary(uid);
		return mapper.writeValueAsString(list);
	}
}













