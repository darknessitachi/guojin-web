package com.xun360.guojin.configure.bean.indecator;

import java.util.ArrayList;
import java.util.List;

import net.jctp.CThostFtdcInstrumentField;

import com.xun360.guojin.configure.bean.ceil.CloseCeil;
import com.xun360.guojin.configure.bean.ceil.WarningCeil;
import com.xun360.guojin.dataexchange.collector.CollectedDataCache;
import com.xun360.guojin.dataexchange.model.InstrumentDetialForInvestor;

/**
 * 品种资金量监控
 * @author Administrator
 *
 */
public class Variety extends Indecator{
	
	private WarningCeil warningCeil;
	private CloseCeil closeCeil;
	//品种号
	private String variety;

	protected String str="品种监控";
	
	private List<String> instumentsCache;

	@Override
	public void riskControl(int clientID) {
		if(instumentsCache == null){
			getInstrumentsByVariety();
		}
		if(!instumentsCache.isEmpty()){
			warningCeil.riskCompute(clientID, str, instumentsCache);;
			closeCeil.riskCompute(clientID, str, instumentsCache);
		}
	}
	
	private List<String> getInstrumentsByVariety(){
		instumentsCache = new ArrayList<String>();
		if(variety != null && !variety.equals("")){
			for(InstrumentDetialForInvestor cThostFtdcInstrumentField : CollectedDataCache.instrumentsMap.values()){
				if(cThostFtdcInstrumentField.ProductID.equals(variety)
						|| variety.equals(cThostFtdcInstrumentField.InstrumentID)){
					instumentsCache.add(cThostFtdcInstrumentField.InstrumentID);
				}
			}
		}
		return instumentsCache;
	}
	
	@Override
	public void setValue(double now, double basic) {
		warningCeil.setNow(now);
		closeCeil.setNow(now);
		warningCeil.setBasic(basic);
		closeCeil.setBasic(basic);
	}
	
	@Override
	public int getStat() {
		return Math.max(warningCeil.getStat(), closeCeil.getStat());
	}
	//构造方法
	public Variety(int id, int type, String criteria){
		this.id=id;
		this.type=type;
		String str[]=criteria.split("\\*");
		this.warningCeil=new WarningCeil(Double.parseDouble(str[1]), 
				Integer.parseInt(str[0]), 
				0,1);
		this.closeCeil=new CloseCeil(Double.parseDouble(str[3]), 
				Integer.parseInt(str[2]), 
				Integer.parseInt(str[4]), 
				Double.parseDouble(str[5]), 
				0,1);
		this.variety=str[6];
		this.str="品种/合约资金监控，品种或合约代码【" + variety + "】，";
	}
	//------------get set-------------
	public String getVariety() {
		return variety;
	}

	public void setVariety(String variety) {
		this.variety = variety;
	}
	public WarningCeil getWarningCeil() {
		return warningCeil;
	}
	public void setWarningCeil(WarningCeil warningCeil) {
		this.warningCeil = warningCeil;
	}
	public CloseCeil getCloseCeil() {
		return closeCeil;
	}
	public void setCloseCeil(CloseCeil closeCeil) {
		this.closeCeil = closeCeil;
	}

	
	
}
