package com.xun360.guojin.util;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
public class TokenInterceptor extends HandlerInterceptorAdapter {

	private JdbcTemplate jdbcTemplate;
	@Override
	public boolean preHandle(final HttpServletRequest request,
			HttpServletResponse response, Object handler) throws Exception {
		System.out.println(handler.getClass().getName());
		if(request.getRequestURI().indexOf("/user/login") > -1){
			return true;
		}
		request.setAttribute("uid", 0);
		request.setAttribute("clientID", 0);
		String sql="SELECT id FROM user WHERE token=?";
		jdbcTemplate.query(sql, new Object[]{request.getParameter("token")},
				new RowCallbackHandler() {
			@Override
			public void processRow(ResultSet rs) throws SQLException {
				request.setAttribute("uid", rs.getInt("id"));
			}
		});
		String clientID = request.getParameter("clientID");
		if(clientID != null && !"".equals(clientID)){
			sql="SELECT userID,investorID FROM client WHERE id=?";
			jdbcTemplate.query(sql, new Object[]{clientID},
					new RowCallbackHandler() {
						@Override
						public void processRow(ResultSet rs) throws SQLException {
							if((int)request.getAttribute("uid")!=0&&(int)request.getAttribute("uid")==rs.getInt("userID")){
								request.setAttribute("clientID", Integer.parseInt(request.getParameter("clientID")));
								request.setAttribute("investorID", rs.getString("investorID"));
							}
						}
					});
		}
		if(request.getRequestURI().indexOf("/admin/")  > -1){
			if((int)request.getAttribute("uid") == -1){
				return true;
			}else{
				return false;
			}
		}
		if((int)request.getAttribute("uid") == 0){
			response.getWriter().write("window.top.location.href='../index.html'");
			return false;
		}
		return true;
	}

	
	public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}
	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}
	
}
