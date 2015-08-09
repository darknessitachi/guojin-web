package com.xun360.guojin.message.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.stereotype.Repository;

import com.xun360.guojin.message.bean.Message;
import com.xun360.guojin.message.bean.MessageResponse;
import com.xun360.guojin.util.Common;

@Repository
public class MessageDao {

	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	public MessageResponse getMessage(int clientID,final int type){
		MessageResponse response=new MessageResponse();
		final List<Message> list=new ArrayList<Message>();
		String sql="SELECT time,content FROM message WHERE clientID=? AND (messageType=? OR ?=0) ORDER BY time DESC LIMIT 0,10";
		jdbcTemplate.query(sql, new Object[]{clientID,type,type},
				new RowCallbackHandler() {
					@Override
					public void processRow(ResultSet rs) throws SQLException {
						Message message=new Message();
						message.setType(type);
						message.setTime(rs.getTimestamp("time"));
						message.setContent(rs.getString("content"));
						list.add(message);
					}
				});
		
		response.setList(list);
		return response;
	}
	
	public static void writeMessage(int clientID,int type,String content,String remark){
		String sql="INSERT INTO message VALUES(NULL,?,?,?,?,?)";
		Common.jdbcTemplate.update(sql,new Object[]{clientID,new Date(),type,content,remark});
	}
}
