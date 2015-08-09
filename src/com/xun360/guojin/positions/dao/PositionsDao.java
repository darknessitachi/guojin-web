package com.xun360.guojin.positions.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.stereotype.Repository;

import com.xun360.guojin.dataexchange.collector.CollectedDataCache;
import com.xun360.guojin.dataexchange.model.WrapAccountPosition;
import com.xun360.guojin.dataexchange.model.WrapAccountPosition.WrapAccountPositionSnapShot;
import com.xun360.guojin.positions.bean.PositionsData;
import com.xun360.guojin.positions.bean.PositionsListResponse;
import com.xun360.guojin.positions.bean.PositionsSummary;
import com.xun360.guojin.util.Common;

@Repository
public class PositionsDao {

	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	private String investorID;
	public PositionsListResponse getPositionsList(int clientID)throws Exception{
		PositionsListResponse response=new PositionsListResponse();
		final List<PositionsData> list=new ArrayList<PositionsData>();
		investorID=Common.getInvestorIDByClientID(clientID);
		List<WrapAccountPosition> collectList=CollectedDataCache.accountPositionDataMap.get(investorID);
		if(collectList!=null){
			for(int i=0;i<collectList.size();i++){
				PositionsData data=new PositionsData();
				WrapAccountPositionSnapShot entry=collectList.get(i).getSnapShot();
				data.setContractID(entry.InstrumentID);
				data.setContractName(entry.ProductID);
				data.setContractType(entry.PosiDirection);
				data.setTotalPositions(entry.Position);
				data.setNowPositions(entry.TdPosition);
				data.setLastPositions(entry.YdPosition);
				data.setUsablePositions(entry.ClosePostion);
				data.setAvePrice(entry.AvgPrice);
				data.setProfit(entry.PositionProfit);
				data.setDeposit(entry.UseMargin);
				list.add(data);
			}
		}
		response.setList(list);
		return response;
	}
	
	public List<PositionsSummary> getSummary(int uid){
		final List<PositionsSummary> response=new ArrayList<PositionsSummary>();
		String sql="SELECT * FROM client WHERE userID=?";
		jdbcTemplate.query(sql, new Object[]{uid},
				new RowCallbackHandler() {
					@Override
					public void processRow(ResultSet rs) throws SQLException {
						String investorID=rs.getString("investorID");
						List<WrapAccountPosition> list=CollectedDataCache.accountPositionDataMap.get(investorID);
						PositionsSummary summary=new PositionsSummary();
						summary.setClientID(rs.getInt("id"));
						summary.setInvestorID(investorID);
						double money=0;
						if(list!=null){
							for(int i=0;i<list.size();i++){
								money+=list.get(i).getSnapShot().UseMargin;
							}
							summary.setNumber(list.size());
						}
						else{
							summary.setNumber(0);
						}
						summary.setMoney(money);
						response.add(summary);
					}
				});
		return response;
	}
}
