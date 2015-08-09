package com.xun360.guojin.configure.bean.ceil;

import java.util.List;

import com.xun360.guojin.util.Common;
import com.xun360.guojin.util.ErrorLog;

/**
 * 警告指标
 * @author Administrator
 *
 */
public class WarningCeil extends Ceil{

	private double value=0;
	private double basic=0;
	private double now=0;
	
	public String toString(){
		StringBuilder builder = new StringBuilder();
		if(type == 1){//按比例
			builder.append("[达到指定比例]").append("比例设置").append("[").append(value).append("%][当前").append((now/basic)*100).append("%]");
		}else if(type == 2){//按精确值
			builder.append("[达到指定值]").append("值设置").append("[").append(value).append("][当前").append(now).append("]");
		}
		return builder.toString();
	}
	
	@Override
	protected Boolean calc() {
		Boolean doAction = false;
		if(type == 0 || basic == 0){
			//意思就是没有设置警告 or 重要数据 如：一般指标中【初始资金】；劣后资金监控中【劣后资金】为0
			this.stat = 0;
			return doAction;
		}
		if((type==1 && (now/basic) < (value/100)) ||
				(type==2 && now<value)){//now<value && direct == -1
			if(direct == -1){//当前值低于设置值 处理
				if(this.stat != 1){//不是已经处于警告状态
					doAction = true;
					this.stat = 1;
				}
			}else{
				this.stat = 0;
			}
		}else {//now>=value 
			if(direct == 1 && now > value){//当前值高于设置值 处理
				if(this.stat != 1){//不是已经处于警告状态
					doAction = true;
					this.stat = 1;
				}
			}else{
				this.stat = 0;
			}
		}
		return doAction;
	}
	
	@Override
	public void riskCompute(int clientID, String indicatorMsg,
			List<String> instruments) {
		if (calc()) {
			String investorID = Common.getInvestorIDByClientID(clientID);
			String msg = "帐号[" + investorID + "]的"
					+ indicatorMsg + ",由于：【" + toString() + "】导致警告";
			ErrorLog.message(clientID, 1, msg, msg);
			log.info(msg);
		}
	}
	
	//构造方法
	public WarningCeil(double value,int type,double basic,int direct){
		this.value=value;
		this.type=type;
		this.basic=basic;
		this.direct = direct;
	}
	//------------get set-------------
	public double getValue() {
		return value;
	}
	public void setValue(double value) {
		this.value = value;
	}

	public double getBasic() {
		return basic;
	}

	public void setBasic(double basic) {
		this.basic = basic;
	}

	public double getNow() {
		return now;
	}

	public void setNow(double now) {
		this.now = now;
	}

}
