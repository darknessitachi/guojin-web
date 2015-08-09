package com.xun360.guojin.configure.bean.ceil;

import java.util.List;

import org.apache.log4j.Logger;


abstract public class Ceil {
	
	protected static Logger log = Logger.getLogger(Ceil.class);
	
	//0:无,1:按比例,2:按数值
	//---或者---
	//0:无,1:警告,2:平仓
	protected int type=0;
	//状态 0:正常,1:警告,2:平仓
	protected int stat=0;
	//  1 高于进行处理 -1 低于进行处理 0无关紧要
	protected int direct = 0;
	
	/**
	 * 获取是否引发警告或者平仓的动作，并且改变一些ceil的基本状态
	 * @return
	 */
	protected abstract Boolean calc();
	
	/**
	 * 风险控制
	 * 1、首先调用 calc
	 * 2、然后实现具体ceil对应的动作
	 * @param clientID
	 * @param indicatorMsg
	 * @param instruments
	 */
	public abstract void riskCompute(int clientID,String indicatorMsg,List<String> instruments);
	
	//------------get set-------------
	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getStat() {
		return stat;
	}

	public void setStat(int stat) {
		this.stat = stat;
	}

	public int getDirect() {
		return direct;
	}

	public void setDirect(int direct) {
		this.direct = direct;
	}

	public class CellCalsResult{
		
		
		public final Boolean doAction;
		
		public final String message;

		public CellCalsResult(Boolean doAction, String message) {
			super();
			this.doAction = doAction;
			this.message = message;
		}
		
	}
	
	
}
