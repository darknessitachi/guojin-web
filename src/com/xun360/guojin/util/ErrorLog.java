package com.xun360.guojin.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;

public class ErrorLog {

	
	
	public static void log(String p1){
		String sql="INSERT INTO error VALUES(NULL,?,?,'','')";
		Common.jdbcTemplate.update(sql,new Object[]{new Date(),p1});
	}
	
	public static void log(String p1,String p2){
		String sql="INSERT INTO error VALUES(NULL,?,?,?,'')";
		Common.jdbcTemplate.update(sql,new Object[]{new Date(),p1,p2});
	}
	
	public static void log(String p1,String p2,String p3){
		String sql="INSERT INTO error VALUES(NULL,?,?,?,?)";
		Common.jdbcTemplate.update(sql,new Object[]{new Date(),p1,p2,p3});
	}
	
	public static void log(String p1,Exception e,String p3){
		String p2="";
		try {  
            StringWriter sw = new StringWriter();  
            PrintWriter pw = new PrintWriter(sw);  
            e.printStackTrace(pw);  
            p2=sw.toString();  
        } catch (Exception e2) {  
            p2="bad getErrorInfoFromException";  
        }  
		String sql="INSERT INTO error VALUES(NULL,?,?,?,?)";
		Common.jdbcTemplate.update(sql,new Object[]{new Date(),p1,p2,p3});
	}

	public static void message(int clientID,int type,String content,String remark){
		String sql="INSERT INTO message VALUES(NULL,?,?,?,?,?)";
		Common.jdbcTemplate.update(sql,new Object[]{clientID,new Date(),type,content,remark});
	}

	
}
