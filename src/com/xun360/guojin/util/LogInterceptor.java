package com.xun360.guojin.util;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
public class LogInterceptor extends HandlerInterceptorAdapter {

	private static final Logger log = Logger.getLogger(LogInterceptor.class);
	
	private String[] exclusive;
	@Override
	public boolean preHandle(final HttpServletRequest request,
			HttpServletResponse response, Object handler) throws Exception {
		for(String excluURL : exclusive){
			if(request.getRequestURL().indexOf(excluURL) > -1){
				return true;
			}
		}
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("url:").append(request.getRequestURL()).append("|")
				.append("ip:").append(getRemoteHost(request)).append("|")
				.append("parameters:[").append(getParameters(request).toString()).append("]|")
				.append("attributes:[").append(getAttributes(request).toString()).append("]|");
		log.info(logBuilder.toString());
		return true;
	}
	
	public void setExclusive(String exclusive) {
		this.exclusive = exclusive.split(",");
	}
	
	public String getRemoteHost(HttpServletRequest request){
	    String ip = request.getHeader("x-forwarded-for");
	    if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)){
	        ip = request.getHeader("Proxy-Client-IP");
	    }
	    if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)){
	        ip = request.getHeader("WL-Proxy-Client-IP");
	    }
	    if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)){
	        ip = request.getRemoteAddr();
	    }
	    return ip.equals("0:0:0:0:0:0:0:1")?"127.0.0.1":ip;
	}
	
	public String getAttributes(HttpServletRequest request){
		StringBuilder builder = new StringBuilder();
		Object clientID = request.getAttribute("clientID");
		Object investorID = request.getAttribute("investorID");
		if(clientID != null){
			builder.append("clientID:").append(clientID).append(",");
		}
		if(investorID != null){
			builder.append("investorID:").append(investorID).append(",");
		}
		return builder.toString();
	}
	
	public String getParameters(HttpServletRequest request){
		StringBuilder builder = new StringBuilder();
		Map<String,String[]> parameters = request.getParameterMap();
		for(String key : parameters.keySet()){
			builder.append(key).append(":");
			for(String value : parameters.get(key)){
				builder.append(value).append("#");
			}
			builder.append(",");
		}
		return builder.toString();
	}
}
