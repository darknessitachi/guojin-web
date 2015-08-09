package com.xun360.guojin.dataexchange.collector;

import org.apache.log4j.Logger;

import com.xun360.guojin.dataexchange.exception.CollectDataException;
import com.xun360.guojin.dataexchange.factory.TraderApiEntryFactory;
import com.xun360.guojin.dataexchange.model.AccountDetail;

public abstract class AbstractTradingCollector implements Runnable,DataCollector{
	
	public static final int RETRY_LIMIT_TIMES = 3;
	
	protected int retryTimes = RETRY_LIMIT_TIMES;//失败重试3次

	protected static Logger log = Logger.getLogger(AbstractTradingCollector.class);
	
	protected AccountDetail account;
	

	public AbstractTradingCollector(AccountDetail account) {
		this.account = account;
	}
	/**
	 * 刷新失败次数
	 */
	public void resetRetryTimes(){
		retryTimes = RETRY_LIMIT_TIMES;
		TraderApiEntryFactory.removeCacheAPI(account);//重置工厂缓存
	}
	
	/**
	 * 返回当前的重试次数
	 * @return
	 */
	public int getCurrentRetryTimes(){
		return retryTimes;
	}
	
	/**
	 * 获取账户信息
	 * @return
	 */
	public AccountDetail getAccountDetail(){
		return account;
	}
	
	@Override
	public void run() {
		try {
			collect();
		} catch (CollectDataException e) {
			throw new IllegalStateException(e.getMessage());
		}
	}
	
	
	
	
}
