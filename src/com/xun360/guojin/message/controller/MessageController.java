package com.xun360.guojin.message.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xun360.guojin.message.service.MessageService;

@Controller
@RequestMapping(value="message")
public class MessageController {

	private ObjectMapper mapper=new ObjectMapper();
	@Autowired
	private MessageService messageService;
	/**
	 * 
	 * @param request.token
	 * @param request.clientID
	 * @param request.type 消息类型 0:全部,1：提醒,2：平仓
	 * 
	 * @return 成功：
	 * 		{
			  "list": [
			    {
			      "time": 1410903384000,
			      "type": 1,
			      "content": "啊哦"
			    },
			    {
			      "time": 1409952968000,
			      "type": 1,
			      "content": "哈哈"
			    }
			  ]
			}
	 * @return 失败：
	 * 	{"list":[]}
	 * @throws JsonProcessingException
	 */
	@RequestMapping(value="/get")
	@ResponseBody
	public String getMessage(HttpServletRequest request,HttpServletResponse response) throws JsonProcessingException{
		
		int clientID=(int)request.getAttribute("clientID");
		int type=Integer.parseInt(request.getParameter("type"));
		return mapper.writeValueAsString(messageService.getMessage(clientID, type));
	}
}
