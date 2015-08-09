package com.xun360.guojin.configure.bean.ceil;

import java.util.List;

import com.xun360.guojin.configure.bean.ClosePosiJob;

/**
 * 平仓指标
 * 
 * @author Administrator
 *
 */
public class CloseCeil extends Ceil {

	private double value = 0;
	// 平仓方式,0:不平,1:全平,2:百分比
	private int way = 0;
	// 百分比比值
	private double percent = 0;
	private double basic = 0;
	private double now = 0;
	
	
	public String toString(){
		StringBuilder builder = new StringBuilder();
		if(way == 1){//1:全平
			builder.append("[全平]");
		}else if(way == 2){//2:百分比
			builder.append("[按百分比平").append(percent).append("%]");
		}
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
			//意思就是没有设置平仓 or 重要数据 如：一般指标中【初始资金】；劣后资金监控中【劣后资金】为0
			this.stat = 0;
			return doAction;
		}
		if((type==1&&(now/basic)<(value/100))||
				(type==2&&now<value)){//如果按比例，就是【当前值】/【基础值】 < 【设置比例值】/100 或者 按绝对值，【当前值】<【设置值】
			if(direct == -1){//当前值低于设置值 处理
				doAction = true;
				this.stat = 2;
			}else{
				this.stat = 0;
			}
		}else {//now>=value 
			if(direct == 1 && now > value){//当前值高于设置值 处理
				doAction = true;
				this.stat = 2;
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
			ClosePosiJob closePosiJob = new ClosePosiJob(clientID, indicatorMsg, instruments,way,percent,this.toString());
			new Thread(closePosiJob) .start();
		}
	}
	
	

	// 构造方法
	public CloseCeil(double value, int type, int way, double percent,
			double basic, int direct) {
		this.value = value;
		this.type = type;
		this.way = way;
		this.percent = percent;
		this.basic = basic;
		this.direct = direct;
	}

	// ------------get set-------------
	public double getValue() {
		return value;
	}

	public void setValue(double value) {
		this.value = value;
	}

	public int getWay() {
		return way;
	}

	public void setWay(int way) {
		this.way = way;
	}

	public double getPercent() {
		return percent;
	}

	public void setPercent(double percent) {
		this.percent = percent;
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
