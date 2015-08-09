package com.xun360.guojin.util;

import java.util.List;

import com.xun360.guojin.configure.bean.ceil.*;
import com.xun360.guojin.dataexchange.exception.ClosePosiException;
import com.xun360.guojin.dataexchange.model.ClosePosiStat;
import com.xun360.guojin.dataexchange.opr.ClosePositionOpr;

@Deprecated
public class InterAction {

	//0:无,1:警告,2:平仓
	private boolean type;
	private String msg="";
	private Ceil ceil;
	
	public void doAction(int clientID,String indecatorMessage,boolean t){
		String str="";
		if(type==t&&ceil.getType()!=0){
			String investorID=Common.getInvestorIDByClientID(clientID);
			msg="帐号ID:"+clientID+"\n帐号:"+investorID+"\n"+indecatorMessage+"\n"+msg;
			if(ceil instanceof WarningCeil){
				if(ceil.getStat()!=1){
					str="警告信息";
				}
				ceil.setStat(1);
			}
			else if(ceil instanceof CloseCeil){
				str="平仓信息";
				ceil.setStat(2);
			}
			else if(ceil instanceof LimitCeil){
				if(ceil.getType()==1&&ceil.getStat()!=1){
					str="警告信息";
				}
				else if(ceil.getType()==2){
					str="平仓信息";
				}
				ceil.setStat(ceil.getType());
			}
			if(str.equals("警告信息")){
				print("警告!!!");
				ErrorLog.message(clientID, 1, "帐号"+investorID+"", msg);
			}
			else if(str.equals("平仓信息")){
				
				synchronized(Common.closeStat){
					Boolean nowStat=Common.closeStat.get(investorID);
					if(nowStat==null||!nowStat){
						print("正在平仓");
						Common.delay(1);
						return ;
					}
					Common.closeStat.put(investorID,true);
				}
				print("平仓!!!");
				ErrorLog.message(clientID, 2, "帐号"+investorID+"", "");
				CloseThread thead=new CloseThread();
				//thead.set
				/*try {
					List<ClosePosiStat> closeList=ClosePositionOpr.fullClosePosi(investorID);
				} catch (ClosePosiException e) {
					e.printStackTrace();
					ErrorLog.log("平仓动作执行错误", e, "");
				}*/
			}
			else{
				print("已发出警告");
			}
			//ErrorLog.log(str,"clientID:"+clientID+",type:"+indecatorMessage,msg);
		}
		if(type!=t){
			ceil.setStat(0);
		}
	}

	private class CloseThread extends Thread{
		@Override
		public void run() {
			// TODO Auto-generated method stub
			super.run();
		}
	}
	
	private void print(String s){
		System.out.println("\n\n"+s+"-->\n"+msg+"\n\n");
	}
	//---------------get set---------------------
	public boolean isType() {
		return type;
	}



	public void setType(boolean type) {
		this.type = type;
	}



	public Ceil getCeil() {
		return ceil;
	}



	public void setCeil(Ceil ceil) {
		this.ceil = ceil;
	}



	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}
	
}
