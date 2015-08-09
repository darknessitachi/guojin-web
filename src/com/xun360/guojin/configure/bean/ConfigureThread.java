package com.xun360.guojin.configure.bean;


public class ConfigureThread extends Thread{
	
	@Override
	public void run() {

		while(true){
			RiskComponent.computeAllInvestorRisk();
			try {
				sleep(3000);
			} catch (InterruptedException e) {
				
			}
		}//whie(true)
	}
	
	
}
