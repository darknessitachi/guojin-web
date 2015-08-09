package com.xun360.guojin.admin.bean;

public class LoginUserInfo {

	/**client id**/
	private int id;
	/**期货帐号**/
	private String name;
	/**期货用户姓名**/
	private String fullName;
	/**期货用户权限**/
	private int permission;
	/**权限登录token**/
	private String token;
	/**重定向ip**/
	private String redirectIp;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getFullName() {
		return fullName;
	}
	public void setFullName(String fullName) {
		this.fullName = fullName;
	}
	public int getPermission() {
		return permission;
	}
	public void setPermission(int permission) {
		this.permission = permission;
	}
	public String getToken() {
		return token;
	}
	public void setToken(String token) {
		this.token = token;
	}
	public String getRedirectIp() {
		return redirectIp;
	}
	public void setRedirectIp(String redirectIp) {
		this.redirectIp = redirectIp;
	}
	
	
}
