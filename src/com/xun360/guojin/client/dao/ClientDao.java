package com.xun360.guojin.client.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import net.jctp.JctpException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.stereotype.Repository;

import com.xun360.guojin.client.bean.ClientBasic;
import com.xun360.guojin.client.bean.ClientData;
import com.xun360.guojin.client.bean.ClientInfoData;
import com.xun360.guojin.client.bean.ClientListResponse;
import com.xun360.guojin.client.bean.FundsResponse;
import com.xun360.guojin.configure.bean.ConfigureInfo;
import com.xun360.guojin.configure.bean.indecator.Indecator;
import com.xun360.guojin.dataexchange.collector.AssistDataCache;
import com.xun360.guojin.dataexchange.collector.CollectedDataCache;
import com.xun360.guojin.dataexchange.factory.TraderApiEntryFactory;
import com.xun360.guojin.dataexchange.model.AccountDetail;
import com.xun360.guojin.dataexchange.model.WrapAccountTradingMoney.WrapAccountTradingMoneySnapShot;
import com.xun360.guojin.util.Common;
import com.xun360.guojin.util.bean.ClientFundsData;


@Repository
public class ClientDao {
	
	private final static ExecutorService threadPool = Executors.newCachedThreadPool();

	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	
	public ClientFundsData addClient(int uid,
						final String name,
						final String password,
						String clientName,
						int initFunds,
						int afterFunds) throws JctpException{
		final ClientFundsData clientFundsData = new ClientFundsData();
		clientFundsData.afterFunds = afterFunds;
		clientFundsData.clientName = clientName;
		clientFundsData.initFunds = initFunds;
		clientFundsData.investorID = name;
		clientFundsData.password = password;
		clientFundsData.uid = uid;
		jdbcTemplate.query("SELECT b.autoConfirm FROM user b where b.id=?", new Object[]{uid},
				new RowCallbackHandler() {
					@Override
					public void processRow(ResultSet rs) throws SQLException {
						clientFundsData.autoConfirm = rs.getInt("autoConfirm");
					}
				});
		//远程验证帐号合法性 ，错误抛出异常
		TraderApiEntryFactory.checkAccountCorrect(new AccountDetail(name, password, clientFundsData.autoConfirm));
		//入库操作
		String sql="INSERT INTO client VALUES(NULL,?,?,?,?,?,?,0,0,0,0,0,0,0)";
		jdbcTemplate.update(sql, 
				new Object[]{uid,
							name,
							password,
							clientName,
							initFunds,
							afterFunds});
		jdbcTemplate.query("SELECT a.id FROM client a WHERE userID=? and investorID=? order by id desc limit 0,1", new Object[]{uid,name},
				new RowCallbackHandler() {
					@Override
					public void processRow(ResultSet rs) throws SQLException {
						clientFundsData.clientID = rs.getInt("id");
					}
				});
		//异步添加数据到AssistDataCache
		threadPool.execute(new Runnable() {
			public void run() {
				AssistDataCache.addClient(name,password,clientFundsData.autoConfirm);
			}
		});
		//同步clientData
		if(clientFundsData.clientID != 0){
			synchronized (Common.sync) {
				Common.clientData.put(clientFundsData.clientID, clientFundsData);
			}
		}
		return null;
	}
	
	public void changeClient(final int clientID,
				final String name,
				final String password,
				String clientName,
				int initFunds,
				int afterFunds){
		final ClientBasic oldClientBasic = getBasic(clientID);
		String sql="UPDATE client "+
					"SET investorID=?, "+
					"password=?, "+
					"clientName=?, "+
					"initFunds=?, "+
					"afterFunds=? "+
					"WHERE id=?";
		jdbcTemplate.update(sql, 
			new Object[]{name,
						password,
						clientName,
						initFunds,
						afterFunds,
						clientID});
		//帐号名有改动
		if(!oldClientBasic.getInvestorID().equals(name)){
			//删除原来的cache
			threadPool.execute(new Runnable() {
				public void run() {
					AssistDataCache.deleteClient(oldClientBasic.getInvestorID());
				}
			});
		}
		//密码有改动
		if(!oldClientBasic.getPassword().equals(password)){
			//更新辅助缓存信息
			threadPool.execute(new Runnable() {
				public void run() {
					AssistDataCache.updateClient(name,password,Common.clientData.get(clientID).autoConfirm);
				}
			});
		}
		//更新内存中的内容
		synchronized (Common.sync) {
			//更新Common.clientData
			final ClientFundsData clientFundsData = Common.clientData.get(clientID);
			clientFundsData.afterFunds = afterFunds;
			clientFundsData.clientName = clientName;
			clientFundsData.initFunds = initFunds;
			clientFundsData.investorID = name;
			clientFundsData.password = password;
			//初始资金 或者 劣后资金 有改动
			if(oldClientBasic.getAfterFunds() != afterFunds 
					|| oldClientBasic.getInitFunds() != initFunds){
				//更新对应的indicator 由于初始资金和劣后资金的变更导致的
				ConcurrentMap<Integer,List<ConfigureInfo>> allUserConfigs = Common.info;
				for(Integer userId : allUserConfigs.keySet()){
					boolean found = false;
					List<ConfigureInfo> configureInfos = allUserConfigs.get(userId);
					for(ConfigureInfo configureInfo : configureInfos){
						if(configureInfo.getClientID() == clientID){
							List<Indecator> indecators = configureInfo.getIndecator();
							for(Indecator indecator : indecators){
								//更新指标
								indecator.updateByClientFundsData(clientFundsData);
							}
							found = true;
							break;
						}
					}
					if(found){
						break;
					}
				}
			}
		}
	}
	
	public ClientListResponse getClientList(final int uid)throws Exception{
		ClientListResponse response=new ClientListResponse();
		final List<ClientData> list=new ArrayList<ClientData>();
		String sql="SELECT id,investorID,clientName FROM client WHERE userID=? ORDER BY id";
		jdbcTemplate.query(sql, new Object[]{uid},
				new RowCallbackHandler() {
					@Override
					public void processRow(ResultSet rs) throws SQLException {
						ClientData data=new ClientData();
						data.setId(rs.getInt("id"));
						data.setName(rs.getString("investorID"));
						data.setClientName(rs.getString("clientName"));
						data.setStat(Common.getCurrentStatByUserIdAndClientID(uid, data.getId()));
						list.add(data);
					}
				});
		response.setList(list);
		return response;
	}
	
//	private String investorID;
	public FundsResponse getFunds(int clientID)throws Exception{
		final FundsResponse response=new FundsResponse();
		String sql="SELECT * FROM client WHERE id=?";
		final StringBuilder investorIDbuilder = new StringBuilder();
		jdbcTemplate.query(sql, new Object[]{clientID},
				new RowCallbackHandler() {
					@Override
					public void processRow(ResultSet rs) throws SQLException {
						response.setInitFunds(rs.getInt("initFunds"));
						investorIDbuilder.append(rs.getString("investorID"));
					}
				});
		String investorID = investorIDbuilder.toString();
		if(CollectedDataCache.accountMoneyDataMap.get(investorID)==null){
			return new FundsResponse();
		}
		WrapAccountTradingMoneySnapShot entry=CollectedDataCache.accountMoneyDataMap.get(investorID).getSnapShot();
		
		response.setDynamicRight(entry.DynamicRight);
		response.setStaticRight(entry.PreBalance);
		response.setProfit(entry.PositionProfit);
		response.setDeposit(entry.CurrMargin);
		response.setExpendableFunds(entry.AvailableCash);
		response.setFreeze(entry.FrozenSubmit);
		response.setRisk(entry.RiskRate);
		return response;
	}
	
	public ClientBasic getBasic(int clientID){
		final ClientBasic response=new ClientBasic();
		String sql="SELECT * FROM client WHERE id=?";
		jdbcTemplate.query(sql, new Object[]{clientID},
				new RowCallbackHandler() {
					@Override
					public void processRow(ResultSet rs) throws SQLException {
						response.setInvestorID(rs.getString("investorID"));
						response.setPassword(rs.getString("password"));
						response.setName(rs.getString("clientName"));
						response.setInitFunds(rs.getDouble("initFunds"));
						response.setAfterFunds(rs.getDouble("afterFunds"));
					}
				});
		return response;
	}
	
	public void deleteClient(final int clientID){
		String sql="DELETE FROM client WHERE id=?";
		jdbcTemplate.update(sql,new Object[]{clientID});
		sql="DELETE FROM configure WHERE clientID=?";
		jdbcTemplate.update(sql,new Object[]{clientID});
		threadPool.execute(new Runnable() {
			public void run() {
				AssistDataCache.deleteClient(Common.getInvestorIDByClientID(clientID));
			}
		});
		synchronized (Common.sync) {
			List<ConfigureInfo> infoList=Common.info.get(Common.getUIDByClientID(clientID));
			Common.clientData.remove(clientID);
			if(infoList!=null){
				for(int i=0;i<infoList.size();i++){
					if(infoList.get(i).getClientID()==clientID){
						infoList.remove(i);
					}
				}
			}
		}
	}

	/**
	 * 根据投顾帐号获取投顾信息
	 * @param name
	 * @return
	 */
	public ClientInfoData getClientByInvestorID(String name) {
		final ClientInfoData clientInfoData = new ClientInfoData();
		String sql="select a.id,name,fullName,investorID,a.password,clientName,initFunds,afterFunds from client a join `user` b on a.userID=b.id and investorID=?";
		jdbcTemplate.query(sql, new Object[]{name},
				new RowCallbackHandler() {
					@Override
					public void processRow(ResultSet rs) throws SQLException {
						clientInfoData.setClientID(rs.getString("id"));
						clientInfoData.setInvestorID(rs.getString("investorID"));
						clientInfoData.setPassword(rs.getString("password"));
						clientInfoData.setName(rs.getString("clientName"));
						clientInfoData.setInitFunds(rs.getDouble("initFunds"));
						clientInfoData.setAfterFunds(rs.getDouble("afterFunds"));
						clientInfoData.setUerName(rs.getString("name")+"（" + rs.getString("fullName") + "）");
					}
				});
		if(clientInfoData.getInvestorID() == null){
			return null;
		}
		return clientInfoData;
	}
}
