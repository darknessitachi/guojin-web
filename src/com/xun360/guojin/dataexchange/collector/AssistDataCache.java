package com.xun360.guojin.dataexchange.collector;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.jdbc.core.RowCallbackHandler;

import com.xun360.guojin.dataexchange.conductor.CollectJobConductor;
import com.xun360.guojin.dataexchange.factory.TraderApiEntryFactory;
import com.xun360.guojin.dataexchange.model.AccountDetail;
import com.xun360.guojin.util.Common;
import com.xun360.guojin.util.SystemConfig;

public class AssistDataCache {
	
	
	/** 合约列表Map **/
	/** key=客户id，value=客户信息 **/
	public final static ConcurrentHashMap<String, AccountDetail> accountMap 
				= new ConcurrentHashMap<String, AccountDetail>();

//	public static void initClient(){
//		String account = "00000000";
//		for (int i = 1; i <= 50; i++) {
//			String ac = account.substring(String.valueOf(i).length()) + i;
//			accountMap.put(ac, new AccountDetail(ac, "123456",0));
//		}
//	}
	public static void initClient(){
		String sql="SELECT a.*,b.autoConfirm FROM client a join user b on a.userID=b.id and b.serverNum=" + SystemConfig.THIS_SERVER_NUM + " ORDER BY a.id";
		Common.jdbcTemplate.query(sql, new RowCallbackHandler() {
			@Override
			public void processRow(ResultSet rs) throws SQLException {
				String investorID=rs.getString("investorID");
				String password=rs.getString("password");
				Integer autoConfirm=rs.getInt("autoConfirm");
				accountMap.put(investorID, new AccountDetail(investorID, password,autoConfirm));
			}
		});
	}
	
	/**
	 * 添加一个帐号信息
	 * @param investorID
	 * @param password
	 * @param autoConfirm 1-自动 0-手动
	 * @return
	 */
	public static boolean addClient(String investorID,String password,Integer autoConfirm){
		AccountDetail accountDetail = new AccountDetail(investorID, password, autoConfirm);
		boolean newInsert =  accountMap.putIfAbsent(investorID, accountDetail) == null ? true : false;
		if(newInsert){//成功添加
			CollectJobConductor.refreshCollectedDataByAccount(investorID);
		}
		return newInsert;
	}
	/**
	 * 如果删除一个数据库中不存在的账户，invetorID为“”；
	 * @param investorID
	 */
	public static void deleteClient(String investorID){
		AccountDetail accountDetail =  accountMap.remove(investorID);
		if(accountDetail != null){
			CollectJobConductor.removeCollectorCache(accountDetail);
			TraderApiEntryFactory.resetAccountInBlackList(accountDetail);
			TraderApiEntryFactory.removeCacheAPI(accountDetail);
		}
	}
	
	/**
	 * 更新一个帐号信息
	 * @param investorID
	 * @param password
	 * @param autoConfirm 1-自动 0-手动
	 */
	public static void updateClient(String investorID,String password,Integer autoConfirm){
		if(!addClient(investorID,password,autoConfirm)){//添加失败，说明已经添加
			AccountDetail accountDetail = accountMap.get(investorID);
			if(password != null && !password.equals(accountDetail.getPassword())){//密码不为空 且作了改变
				accountDetail.setPassword(password);//更新密码
				TraderApiEntryFactory.resetAccountInBlackList(accountDetail);//重置黑名单
				TraderApiEntryFactory.removeCacheAPI(accountDetail);//重置帐号缓存
			}
			if(autoConfirm != null){
				accountDetail.setAutoConfirm(autoConfirm);//更新结算确认状态
			}
//			CollectJobConductor.removeCollectorCache(accountDetail);//更新api缓存
//			CollectJobConductor.refreshCollectedDataByAccount(investorID);//获取行情和持仓
		}
	}
	
}
