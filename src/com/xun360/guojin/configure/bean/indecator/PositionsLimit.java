package com.xun360.guojin.configure.bean.indecator;

import java.util.ArrayList;
import java.util.List;

import com.xun360.guojin.configure.bean.ceil.LimitCeil;
import com.xun360.guojin.util.ErrorLog;

/**
 * 持仓控制
 * @author Administrator
 *
 */
public class PositionsLimit extends Indecator{

	private List<LimitCeil> ceilList;
	
	protected String str;
	
	@Override
	public void riskControl(int clientID) {
		for(int i=0;i<ceilList.size();i++){
			ceilList.get(i).riskCompute(clientID, str, null);
		}
	}
	
	public void setHasPosi(boolean has){
		for(int i=0;i<ceilList.size();i++){
			ceilList.get(i).setHasPosi(has);
		}
	}
	
	@Override
	public void setValue(double now, double basic) {
		ErrorLog.log("设置了多余的参数","PositionLimit","now:"+now+",basic:"+basic);
	}
	
	@Override
	public int getStat() {
		int stat=0;
		for(int i=0;i<ceilList.size();i++){
			stat=Math.max(stat, ceilList.get(i).getStat());
		}
		return stat;
	}
	//构造方法
	public PositionsLimit(int id,int type,String criteria){
		this.id=id;
		this.type=type;
		String str[]=criteria.split("\\*");
		ceilList=new ArrayList<LimitCeil>();
		for(int i=0;i<3;i++){
			LimitCeil ceil=new LimitCeil(Integer.parseInt(str[i*3]), 
					Integer.parseInt(str[i*3+1]), 
					Integer.parseInt(str[i*3+2]),false);
			ceilList.add(ceil);
		}
		this.str="自定义时间段限制开仓监控，";
	}
	//------------get set-------------

	public List<LimitCeil> getCeilList() {
		return ceilList;
	}

	public void setCeilList(List<LimitCeil> ceilList) {
		this.ceilList = ceilList;
	}

	
}
