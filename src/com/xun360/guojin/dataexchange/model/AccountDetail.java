package com.xun360.guojin.dataexchange.model;

public class AccountDetail {

	/**账户用户名**/
	private String userId;
	/**账户密码**/
	private String password;
	/**自动确认结算单 1-自动 0-手动**/
	private Integer autoConfirm;
	
	public AccountDetail(String userId, String password, Integer autoConfirm) {
		super();
		this.userId = userId;
		this.password = password;
		this.autoConfirm = autoConfirm;
	}
	
	public String toString(){
		return userId;
	}

	public AccountDetail() {
		super();
		// TODO Auto-generated constructor stub
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getPassword() {
		return password;
	}

	public synchronized void setPassword(String password) {
		this.password = password;
	}

	public Integer getAutoConfirm() {
		return autoConfirm;
	}

	public synchronized void setAutoConfirm(Integer autoConfirm) {
		this.autoConfirm = autoConfirm;
	}

	@Override
	public int hashCode() {
		return this.userId.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return userId.equals(((AccountDetail)obj).getUserId());
	}
	
	
	
	
}
