package com.xun360.guojin.admin.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.stereotype.Repository;

import com.xun360.guojin.admin.bean.GroupData;
import com.xun360.guojin.admin.bean.ManagerData;
import com.xun360.guojin.configure.bean.ConfigureInfo;
import com.xun360.guojin.dataexchange.collector.AssistDataCache;
import com.xun360.guojin.util.Common;
import com.xun360.guojin.util.SystemConfig;
import com.xun360.guojin.util.bean.ClientFundsData;

@Repository
public class AdminDao {

	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	//添加分组
	public String addGroup(String name,int maxUser){
		String sql="INSERT INTO `group` VALUES(NULL,?,?,NOW())";
		jdbcTemplate.update(sql,new Object[]{name,maxUser});
		return "1";
	}
	
	//修改分组
	public String changeGroup(int id,String name,int maxUser){
		String sql="UPDATE `group` SET name=?,maxUser=? WHERE id=?";
		jdbcTemplate.update(sql,new Object[]{name,maxUser,id});
		return "1";
	}
	
	//删除分组
	public String deleteGroup(int id){
		String sql="DELETE FROM `group` WHERE id=?";
		jdbcTemplate.update(sql,new Object[]{id});
		return "1";
	}
	
	//获取分组
	public List<GroupData> getGroup(){
		final List<GroupData> list=new ArrayList<GroupData>();
		String sql="SELECT id,name,maxUser,createTime,count FROM `group` LEFT OUTER JOIN ( "+
				"SELECT COUNT(*)AS count,groupID FROM `user` "+
				"GROUP BY groupID "+
				")AS u "+
				"ON groupID=id "+
				"ORDER BY id";
		jdbcTemplate.query(sql, new RowCallbackHandler() {
			@Override
			public void processRow(ResultSet rs) throws SQLException {
				GroupData data=new GroupData();
				data.setId(rs.getInt("id"));
				data.setName(rs.getString("name"));
				data.setMax(rs.getInt("maxUser"));
				data.setNow(rs.getInt("count"));
				data.setCreateTime(rs.getTimestamp("createTime"));
				list.add(data);
			}
		});
		return list;
	}
	//------------------------user----------------------------
	
	//获取投顾
	public List<ManagerData> getManager(String groupId){
		String sql="select c.id AS uid,c.name AS uname,fullName,groupID,permission,e.name AS gname,maxUser,case when ISNULL(count) then 0 else count end as count,c.autoConfirm "+
				"from `user` c left join (select b.userId,count(1) count from `user` a join client b on a.id=b.userID group by b.userID) d on c.id=d.userId " +
				"join `group` e on c.groupID=e.id ";
		if(groupId != null && !"".equals(groupId)){
			sql += " where e.id=" + groupId;
		}
		final List<ManagerData> list=new ArrayList<ManagerData>();
		jdbcTemplate.query(sql, new RowCallbackHandler() {
			@Override
			public void processRow(ResultSet rs) throws SQLException {
				ManagerData data=new ManagerData();
				data.setId(rs.getInt("uid"));
				data.setName(rs.getString("uname"));
				data.setFullName(rs.getString("fullName"));
				data.setGroupID(rs.getInt("groupID"));
				data.setGroupName(rs.getString("gname"));
				data.setMax(rs.getInt("maxUser"));
				data.setNow(rs.getInt("count"));
				data.setPermission(rs.getInt("permission"));
				data.setAutoConfirm(rs.getInt("autoConfirm"));
				list.add(data);
			}
		});
		return list;
	}
	
	//修改投顾
	public String changeManager(int id,String name,String fullName,String password,int groupID,int permission, int autoConfirm){
		if(!checkName(name)){
			return "0";
		}
		final List<Integer> clientIDs = new ArrayList<Integer>();
		String sql = "select b.id as clientID,autoConfirm from `user` a join client b on a.id=b.userID where a.id=?";
		final AtomicInteger orgAutoConfirm = new AtomicInteger(autoConfirm);
		jdbcTemplate.query(sql, new Object[]{id},new RowCallbackHandler() {
			@Override
			public void processRow(ResultSet rs) throws SQLException {
				orgAutoConfirm.set(rs.getInt("autoConfirm"));
				clientIDs.add(rs.getInt("clientID"));
			}
		});
		if(password == null){
			sql="UPDATE `user` SET name=?,fullName=?,groupID=?,permission=?,autoConfirm=? WHERE id=?";
			jdbcTemplate.update(sql,new Object[]{name,fullName,groupID,permission,autoConfirm,id});
		}else{
			sql="UPDATE `user` SET name=?,fullName=?,password=?,groupID=?,permission=?,autoConfirm=? WHERE id=?";
			jdbcTemplate.update(sql,new Object[]{name,fullName,password,groupID,permission,autoConfirm,id});
		}
		if(orgAutoConfirm.get() != autoConfirm){
			//如果确认结算方式改变，更新Common.info的对应帐号的方式
			synchronized (Common.sync) {
				for(Integer clientID : clientIDs){
					//改变clientData
					ClientFundsData clientFundsData = Common.clientData.get(clientID);
					clientFundsData.autoConfirm = autoConfirm;
					//改变AssistDataCache中的AccountDetails的autoConfirm
					AssistDataCache.updateClient(clientFundsData.investorID, null, autoConfirm);
				}
			}
		}
		return "1";
	}

	//删除投股
	public String deleteManager(int id){
		String sql="DELETE FROM `user` WHERE id=?";
		jdbcTemplate.update(sql,new Object[]{id});
		return "1";
	}
	
	//添加投顾
	public String addManager(String name,String fullName,String password,int groupID,int permission, int autoConfirm){
		if(!checkName(name)){
			return "0";
		}
		if(!checkRepeat(name)){
			return "0";
		}
		String sql="INSERT INTO `user` VALUES(NULL,?,?,?,?,?,?,?,?)";
		jdbcTemplate.update(sql,new Object[]{fullName,name,password,password,groupID,permission,autoConfirm,new Random().nextInt(SystemConfig.TOTAL_NUM)+1});
		return "1";
	}
	
	//验证帐号合法性
	private boolean checkName(String name){
		if(name.length()>20){
			return false;
		}
		Pattern pattern=Pattern.compile("^[a-z|A-Z|0-9]+$");
		if(!pattern.matcher(name).find()){
			return false;
		}
		return true;
	}
	
	private boolean checkRepeat(String name){
		final Check check=new Check();
		String sql="SELECT * FROM user WHERE name=?";
		jdbcTemplate.query(sql, new Object[]{name},
				new RowCallbackHandler() {
					@Override
					public void processRow(ResultSet arg0) throws SQLException {
						check.setCheck(false);
					}
				});
		return check.isCheck();
	}
	private class Check{
		private boolean check=true;

		public boolean isCheck() {
			return check;
		}

		public void setCheck(boolean check) {
			this.check = check;
		}
		
	}
	
}
