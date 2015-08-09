package com.xun360.guojin.admin.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.stereotype.Repository;

import com.xun360.guojin.admin.bean.LoginUserInfo;
import com.xun360.guojin.util.MD5Util;
import com.xun360.guojin.util.SystemConfig;

@Repository
public class UserDao {

	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	public LoginUserInfo login(final String name,String password){
		final StringBuilder token= new StringBuilder("");
		String sql="SELECT id FROM user WHERE name=? AND password=? and permission in (7,6,5,4)";
		jdbcTemplate.query(sql, new Object[]{name,password},
				new RowCallbackHandler() {
					@Override
					public void processRow(ResultSet rs) throws SQLException {
						token.append(MD5Util.generatePassword(name+(new Date()).getTime()+rs.getInt("id")));
						String sql="UPDATE `user` SET token=? WHERE id=?";
						jdbcTemplate.update(sql,new Object[]{token.toString(),rs.getInt("id")});
					}
				});
		final LoginUserInfo loginUserInfo = new LoginUserInfo();
		if(!token.toString().equals("")){
			sql = "select * from user where name=? limit 0,1";
			jdbcTemplate.query(sql, new Object[]{name},
				new RowCallbackHandler() {
					public void processRow(ResultSet rs) throws SQLException {
						loginUserInfo.setId(rs.getInt("id"));
						loginUserInfo.setName(rs.getString("name"));
						loginUserInfo.setFullName(rs.getString("fullName"));
						loginUserInfo.setPermission(rs.getInt("permission"));
						loginUserInfo.setToken(token.toString());
						int redirectServerNum = rs.getInt("serverNum");
						if(redirectServerNum > 0){
							try {
								String redirectIp = (String) SystemConfig.class.getDeclaredField("SERVER_" + redirectServerNum + "_IP").get(SystemConfig.class);
								loginUserInfo.setRedirectIp(redirectIp);
							} catch (IllegalArgumentException e) {
								e.printStackTrace();
							} catch (IllegalAccessException e) {
								e.printStackTrace();
							} catch (NoSuchFieldException e) {
								e.printStackTrace();
							} catch (SecurityException e) {
								e.printStackTrace();
							}
						}
					}
				}
			);
		}
		return loginUserInfo;
	}
	
	public String regist(String name,String password){
		if(name.length()>20){
			return "0";
		}
		Pattern pattern=Pattern.compile("^[a-z|A-Z|0-9]+$");
		if(!pattern.matcher(name).find()){
			return "0";
		}
		
		final AtomicBoolean check= new AtomicBoolean(false);
		String sql="SELECT * FROM user WHERE name=?";
		jdbcTemplate.query(sql, new Object[]{name},
				new RowCallbackHandler() {
					@Override
					public void processRow(ResultSet arg0) throws SQLException {
						check.set(true);;
					}
				});
		if(check.get()){
			return "0";
		}

		sql="INSERT INTO `user` VALUES(NULL,?,?,?)";
		jdbcTemplate.update(sql,new Object[]{name,password,password});
		return "1";
	}

	public boolean modifyPassword(String name, String oldPassword,
			String newPassword) {
		String sql="update `user` set name=?,password=? where name=? and password=?";
		int changeRows = jdbcTemplate.update(sql,new Object[]{name,newPassword,name,oldPassword});
		return changeRows == 0 ? false : true;
	}
}
