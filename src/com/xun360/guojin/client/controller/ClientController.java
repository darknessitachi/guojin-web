package com.xun360.guojin.client.controller;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.jctp.JctpException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xun360.guojin.client.bean.ClientBasic;
import com.xun360.guojin.client.bean.ClientInfoData;
import com.xun360.guojin.client.bean.ClientListResponse;
import com.xun360.guojin.client.bean.FundsResponse;
import com.xun360.guojin.client.service.ClientService;
import com.xun360.guojin.configure.bean.RiskComponent;
import com.xun360.guojin.dataexchange.conductor.CollectJobConductor;
import com.xun360.guojin.util.DESUtils;
import com.xun360.guojin.util.bean.ClientFundsData;

@Controller
@RequestMapping(value = "client")
public class ClientController {

	private ObjectMapper mapper = new ObjectMapper();
	@Autowired
	private ClientService clientService;
	
	private DESUtils desUtils = new DESUtils();

	/**
	 * 添加期货客户
	 * 
	 * @param request
	 *            .token 账号验证令牌
	 * @param request
	 *            .name 客户账号
	 * @param request
	 *            .password 客户密码
	 * @param request
	 *            .clientName 姓名
	 * @param request
	 *            .initFunds 初始资金
	 * @param request
	 *            .afterFunds 劣后资金:-1为没有劣后资金
	 * 
	 * @return 成功 :1
	 * @return 失败:0
	 */
	@RequestMapping(value = "/addClient")
	@ResponseBody
	@Deprecated
	public String addClient(HttpServletRequest request,
			HttpServletResponse response) {
		try {
			int uid = (int) request.getAttribute("uid");
			String name = request.getParameter("name");
			String password = request.getParameter("password");
			String clientName = request.getParameter("clientName");
			int initFunds = Integer.parseInt(request.getParameter("initFunds"));
			int afterFunds = 0;
			if (request.getParameter("afterFunds") != null
					&& !"".equals(request.getParameter("afterFunds"))) {
				afterFunds = Integer.parseInt(request
						.getParameter("afterFunds"));
			}
			clientService.addClient(uid, name, password, clientName, initFunds,
					afterFunds);
			// 风控
			RiskComponent.computeAllInvestorRisk();
			return "1";
		} catch (Exception e) {
			return "0";
		}
	}

	@RequestMapping(value = "/add")
	@ResponseBody
	public String add(HttpServletRequest request) throws JsonProcessingException {
		Map<String,String> returnMap = new HashMap<String, String>();
		try {
			int uid = (int) request.getAttribute("uid");
			String name = request.getParameter("name");
			String password = desUtils.strDec(request.getParameter("password"));
			String clientName = request.getParameter("clientName");
			int initFunds = Integer.parseInt(request.getParameter("initFunds"));
			int afterFunds = 0;
			if (request.getParameter("afterFunds") != null
					&& !"".equals(request.getParameter("afterFunds"))) {
				afterFunds = Integer.parseInt(request
						.getParameter("afterFunds"));
			}
			ClientInfoData clientInfoData = getClientByInvestorID(name);
			if (clientInfoData != null) {
				returnMap.put("msg", "期货帐号【" + clientInfoData.getInvestorID() + "】已经被投顾【"
						+ clientInfoData.getUerName() + "】管理中，无法添加!");
				
			} else {
				ClientFundsData clientFundsData = clientService.addClient(uid,
						name, password, clientName, initFunds, afterFunds);
				// 风控
				RiskComponent.computeAllInvestorRisk();
				returnMap.put("msg", "1");
			}
		} catch (JctpException e){
			e.printStackTrace();
			returnMap.put("msg", e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			returnMap.put("msg", "0");
		} finally{
			return mapper.writeValueAsString(returnMap);
		}
	}

	@RequestMapping(value = "/change")
	@ResponseBody
	public String change(HttpServletRequest request) throws JsonProcessingException {
		Map<String,String> returnMap = new HashMap<String, String>();
		try {
			int clientID = (int) request.getAttribute("clientID");
			String name = request.getParameter("name");
			String password = desUtils.strDec(request.getParameter("password"));
			String clientName = request.getParameter("clientName");
			int initFunds = Integer.parseInt(request.getParameter("initFunds"));
			int afterFunds = 0;
			if (request.getParameter("afterFunds") != null
					&& !"".equals(request.getParameter("afterFunds"))) {
				afterFunds = Integer.parseInt(request
						.getParameter("afterFunds"));
			}
			ClientInfoData clientInfoData = getClientByInvestorID(name);
			if (clientInfoData != null
					&& !clientInfoData.getClientID().equals(String.valueOf(clientID))) {// 判断期货帐号是否被添加
				 returnMap.put("msg", "期货帐号【" + clientInfoData.getInvestorID() + "】已经被投顾【"
						+ clientInfoData.getUerName() + "】管理中，无法添加!");
			} else {
				clientService.changeClient(clientID, name, password,
						clientName, initFunds, afterFunds);
				returnMap.put("msg","1");
				// 风控
				RiskComponent.computeAllInvestorRisk();
			}
		} catch (Exception e) {
			e.printStackTrace();
			returnMap.put("msg","0");
		} finally{
			return mapper.writeValueAsString(returnMap);
		}
	}

	public ClientInfoData getClientByInvestorID(String name) {
		return clientService.getClientByInvestorID(name);
	}

	// 参数比上面的addclient多一个clientID
	@RequestMapping(value = "/changeClient")
	@ResponseBody
	@Deprecated
	public String changeClient(HttpServletRequest request,
			HttpServletResponse response) {
		try {
			int clientID = (int) request.getAttribute("clientID");
			String name = request.getParameter("name");
			String password = desUtils.strDec(request.getParameter("password"));
			String clientName = request.getParameter("clientName");
			int initFunds = Integer.parseInt(request.getParameter("initFunds"));
			int afterFunds = Integer.parseInt(request
					.getParameter("afterFunds"));
			clientService.changeClient(clientID, name, password, clientName,
					initFunds, afterFunds);
			// 风控
			RiskComponent.computeAllInvestorRisk();
			return "1";

		} catch (Exception e) {
			e.printStackTrace();
			return "0";
		}
	}

	/**
	 * 查询投顾账号下的客户期货账号列表
	 * 
	 * @param request
	 *            .token 账号验证令牌
	 * 
	 * @return成功: { "list": [ { "id": 2, //客户期货账号ID "name": "sdf", //客户期货账号名
	 *            "clientName": "xc"//客户姓名 }, { "id": 3, "name": "fff",
	 *            "clientName": "xiechao" } ] }
	 * @return 失败:0
	 */
	@RequestMapping(value = "getClient")
	@ResponseBody
	public String getClientList(HttpServletRequest request,
			HttpServletResponse response) throws Exception {

		int uid = (int) request.getAttribute("uid");
		ClientListResponse res = clientService.getClientList(uid);

		return mapper.writeValueAsString(res);
	}

	/**
	 * 查询资金情况
	 * 
	 * @param request
	 *            .token 账号验证令牌
	 * @param request
	 *            .clientID 客户期货账号ID
	 * 
	 * @return 成功: { "initFunds": 5.0E8,//初始资金 "profit":
	 *         5471925.000000004,//持仓盈亏 "dynamicRight":
	 *         4.1563177499999994E8,//动态权益 "staticRight": 5.0E8,//静态权益
	 *         "deposit": 5.970333999999996E7,//占用保证金 "expendableFunds":
	 *         3.55049969E8,//可用资金 "freeze": 878465.9999999776,//下单冻结 "risk":
	 *         0.16815475345105568//风险度,16.82% }
	 */
	@RequestMapping(value = "/getFunds")
	@ResponseBody
	public String getFunds(HttpServletRequest request,
			HttpServletResponse response) throws Exception {

		int clientID = (int) request.getAttribute("clientID");
		FundsResponse res = clientService.getFunds(clientID);
		return mapper.writeValueAsString(res);
	}
	
	@RequestMapping(value = "/refreshFunds")
	@ResponseBody
	public String refreshFunds(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		if(request.getAttribute("investorID") != null){
			CollectJobConductor.refreshAccountMoneyByAccount(String.valueOf(request.getAttribute("investorID")));
		}
		int clientID = (int) request.getAttribute("clientID");
		FundsResponse res = clientService.getFunds(clientID);
		return mapper.writeValueAsString(res);
	}

	/**
	 * 查询基本信息
	 * 
	 * @param request
	 *            .token 账号验证令牌
	 * @param request
	 *            .cliendID 客户期货账号ID
	 * 
	 * @return 成功: { "investorID": "00000016", "password": "67", "name": "73",
	 *         "initFunds": 5.0E8, "afterFunds": 11.0 }
	 * @return 失败:0
	 */
	@RequestMapping(value = "/getBasic")
	@ResponseBody
	public String getBaisc(HttpServletRequest request,
			HttpServletResponse response) throws Exception {

		int clientID = (int) request.getAttribute("clientID");
		ClientBasic clientBasic =  clientService.getBasic(clientID);
		clientBasic.setPassword(desUtils.strEnc(clientBasic.getPassword()));
		return mapper.writeValueAsString(clientBasic);
	}

	/**
	 * 删除账户
	 * 
	 * @param request
	 *            .token 账号验证令牌
	 * @param request
	 *            .cliendID 客户期货账号ID
	 * 
	 * @return 成功:1
	 * @return 失败:0
	 */
	@RequestMapping(value = "/delete")
	@ResponseBody
	public String deleteClient(HttpServletRequest request,
			HttpServletResponse response) {

		int clientID = (int) request.getAttribute("clientID");
		clientService.deleteClient(clientID);
		return "1";
	}
}
