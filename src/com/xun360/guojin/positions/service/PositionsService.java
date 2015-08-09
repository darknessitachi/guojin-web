package com.xun360.guojin.positions.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.xun360.guojin.positions.bean.PositionsListResponse;
import com.xun360.guojin.positions.bean.PositionsSummary;
import com.xun360.guojin.positions.dao.PositionsDao;

@Service
public class PositionsService {

	@Autowired
	private PositionsDao positionsDao;
	
	public PositionsListResponse getPositionsList(int clientID) throws Exception{
		
		return positionsDao.getPositionsList(clientID);
	}
	
	public List<PositionsSummary> getSummary(int uid){
		
		return positionsDao.getSummary(uid);
	}
}
