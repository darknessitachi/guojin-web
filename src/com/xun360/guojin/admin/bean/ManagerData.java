package com.xun360.guojin.admin.bean;

public class ManagerData {
	private int id;
	private String fullName;
	private String name;
	private int groupID;
	private String groupName;
	private int max;
	private int now;
	private int permission;
	private int autoConfirm;
	
	public String getGroupName() {
		return groupName;
	}
	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}
	public int getMax() {
		return max;
	}
	public void setMax(int max) {
		this.max = max;
	}
	public int getNow() {
		return now;
	}
	public void setNow(int now) {
		this.now = now;
	}
	public String getFullName() {
		return fullName;
	}
	public void setFullName(String fullName) {
		this.fullName = fullName;
	}
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

	public int getGroupID() {
		return groupID;
	}
	public void setGroupID(int groupID) {
		this.groupID = groupID;
	}
	public int getPermission() {
		return permission;
	}
	public void setPermission(int permission) {
		this.permission = permission;
	}
	public int getAutoConfirm() {
		return autoConfirm;
	}
	public void setAutoConfirm(int autoConfirm) {
		this.autoConfirm = autoConfirm;
	}
	
}
