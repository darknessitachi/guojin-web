package net.jctp;

import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;


public class TraderApi extends JctpApi {
    
    private static class SyncRequest{
        int requestId;
        boolean waitUntilLast;
        LinkedList rspFields = new LinkedList();
        CThostFtdcRspInfoField rspInfoField;
        volatile boolean responseReady;
        
        SyncRequest(int requestId, boolean waitForAll){
            this.requestId = requestId;
            this.waitUntilLast = waitForAll;
        }
    }
    
    private TraderApiListener listener = new TraderApiAdapter();
    private String tradingDay;
    private long lastReqQryTime;
    private boolean autoSleepReqQry = false;
    private int syncReqTimeout = 30;
    private ConcurrentHashMap<Integer,SyncRequest> syncRequestMap = new ConcurrentHashMap<Integer,SyncRequest>();
    private SyncRequest connectRequest;
    
    private String confirmDay;
    
    public TraderApi(){
        this("");
    }
    
    public TraderApi(String flowPath) {
        nativeApiPtr = JctpApi.traderapiCreateApi(this,str2c(flowPath));
        if ( nativeApiPtr==0 )
            throw new RuntimeException("Create TraderApi failed");
    }
    
    public void setSyncReqTimeout(int seconds){
        syncReqTimeout = seconds;
    }
    
    public void setAutoSleepReqQry(boolean v){
        this.autoSleepReqQry = v;
    }
    
    private synchronized void autoSleepReqQry(){
        if ( !autoSleepReqQry )
            return;
        long time = System.currentTimeMillis();
        long t = (time-lastReqQryTime);
        if ( t<1000 ){
            try {
                Thread.sleep(1000-t);
            } catch (InterruptedException e) {}
        }
        lastReqQryTime = System.currentTimeMillis();
    }

    private SyncRequest createSyncRequest(boolean waitForAll){
        SyncRequest req = new SyncRequest(getNextRequestId(), waitForAll);
        syncRequestMap.put(req.requestId, req);
        return req;
    }
    
    private void removeSyncRequest(SyncRequest req){
        syncRequestMap.remove(req.requestId);
    }
    
    private Object waitForRsp(SyncRequest req) throws JctpException {
        LinkedList rsps = waitForAllRsps(req);
        if ( rsps!=null )
            return rsps.poll();
        return null;
    }
    
    private LinkedList waitForAllRsps(SyncRequest syncReq) throws JctpException {
        if ( !syncReq.responseReady ){
            synchronized( syncReq ){
                if ( !syncReq.responseReady ){
                    try {
                        syncReq.wait(syncReqTimeout*1000);
                    } catch (InterruptedException e) {}
                }
            }
        }
        CThostFtdcRspInfoField rspInfo = syncReq.rspInfoField;
        if ( !syncReq.responseReady ){
            throw new JctpException(JctpException.ERROR_SYNC_REQUEST_TIMEOUT);
        }
        if ( rspInfo!=null && rspInfo.ErrorID!=0 ){
            throw new JctpException(rspInfo.ErrorID,rspInfo.ErrorMsg);
        }
        if ( syncReq.rspFields.size()==1 && syncReq.rspFields.get(0)==null ){
            return new LinkedList();
        }
        return syncReq.rspFields;
    }
    
    private void notifyRequest(SyncRequest req, CThostFtdcRspInfoField rspInfo){
        req.responseReady = true;
        req.rspInfoField = rspInfo;
        synchronized(req){
            req.notify();
        }
    }
    
    private void notifyRequest(Object rspField, CThostFtdcRspInfoField rspInfo, int nRequest, boolean isLast){
        SyncRequest req = syncRequestMap.get(nRequest);
        if ( req!=null ){
            synchronized(req){
                req.rspFields.add(rspField);
                req.rspInfoField = rspInfo;
                if ( (!req.waitUntilLast) || (req.waitUntilLast&&isLast) ){
                    req.responseReady = true;
                    req.notify();
                }
            }
        }
    }
    
    public void setListener(TraderApiListener listener){
        this.listener = listener;
    }
    
    public TraderApiListener getListener() {
		return listener;
	}

	public String GetTradingDay()
        throws JctpException
    {
        checkConnected();
        if ( tradingDay==null ){
            byte[] data = JctpApi.traderapiGetTradingDay(this,nativeApiPtr);
            if ( data!=null)
                tradingDay = c2str(data, 0, data.length);
        }
        return tradingDay;
    }

    /**
     * 初始化并等待OnFrontConnected消息
     * <BR>初始化运行环境,只有调用后,接口才开始工作
     * 
     * @param frontUrl 前置机网络地址。
     * 网络地址的格式为：“protocol://ipaddress:port”，如：”tcp://127.0.0.1:17001”
     */
    public void SyncConnect(String frontUrl)
        throws JctpException
    {
        connectRequest = new SyncRequest(0,false);
        try{
            Connect(frontUrl);
            waitForRsp(connectRequest);
        }finally{
            connectRequest = null;
        }
    }
    
    /**
     * 初始化.
     * <BR>初始化运行环境,只有调用后,接口才开始工作
     * 
     * @param frontUrl 前置机网络地址。
     * 网络地址的格式为：“protocol://ipaddress:port”，如：”tcp://127.0.0.1:17001”
     */
    public void Connect(String frontUrl){
        JctpApi.traderapiConnect(this,nativeApiPtr,str2c(frontUrl));
    }
    
    public void Close(){
        if ( nativeApiPtr!=0 )
            JctpApi.traderapiClose(this,nativeApiPtr);
    }
    
    /**
     * 订阅私有流。
     * <BR>该方法要在Connect方法前调用。若不调用则不会收到私有流的数据。
     * 
     * @param resumeType 私有流重传方式  
     * THOST_TERT_RESTART:从本交易日开始重传
     * THOST_TERT_RESUME:从上次收到的续传
     * THOST_TERT_QUICK:只传送登录后私有流的内容
     */
    public void SubscribePrivateTopic(int resumeType){
        JctpApi.traderapiSubscribeTopic(this,nativeApiPtr, false, resumeType);
    }
    
    /**
     * 订阅公共流。
     * <BR>该方法要在Connect方法前调用。若不调用则不会收到私有流的数据。
     * 
     * @param resumeType 私有流重传方式  
     * THOST_TERT_RESTART:从本交易日开始重传
     * THOST_TERT_RESUME:从上次收到的续传
     * THOST_TERT_QUICK:只传送登录后私有流的内容
     */
    public void SubscribePublicTopic(int resumeType){
        JctpApi.traderapiSubscribeTopic(this,nativeApiPtr, true, resumeType);
    }

    //--------------------- native callback methods -------------
    
    private void OnFrontConnected()
    {
        connected = true;
        login = false;
        if ( connectRequest!=null ){
            notifyRequest(connectRequest, new CThostFtdcRspInfoField());
        }
        if ( listener!=null )
            listener.OnFrontConnected();
    }
    
    private void OnFrontDisconnected(int nReason)
    {
        tradingDay = null;
        connected = false;
        login = false;
        if ( listener!=null )
            listener.OnFrontDisconnected(nReason);
    }
    
    private void OnHeartBeatWarning(int nTimeLapse){
        if ( listener!=null )
            listener.OnHeartBeatWarning(nTimeLapse);
    }
    

    //---- Req methods
	/** 客户端认证请求 */
	public int ReqAuthenticate(CThostFtdcReqAuthenticateField f) throws JctpException {
		int reqId = getNextRequestId();
		ReqAuthenticate(f,reqId);
		return reqId;
	}
	/** 客户端认证请求 */
	public void ReqAuthenticate(CThostFtdcReqAuthenticateField f, int nRequestID) throws JctpException {
		checkConnected();
		byte[] a1 = CThostFtdcReqAuthenticateField.toBytes(f);
		int r = JctpApi.traderapiReq(this,nativeApiPtr, MethodId_TraderApi_ReqAuthenticate, a1, nRequestID);
		if ( r!=0 ) throw new JctpException(r);
	}
	/** 客户端认证请求(SYNC) */
	public CThostFtdcRspAuthenticateField SyncReqAuthenticate(CThostFtdcReqAuthenticateField f) throws JctpException {
		SyncRequest syncReq = createSyncRequest(false);
		try{
			ReqAuthenticate(f,syncReq.requestId);
			return (CThostFtdcRspAuthenticateField)waitForRsp(syncReq);
		}finally{ removeSyncRequest(syncReq); }
	}
	/** 客户端认证请求(SYNC ALL) */
	public CThostFtdcRspAuthenticateField[] SyncAllReqAuthenticate(CThostFtdcReqAuthenticateField f) throws JctpException {
		SyncRequest syncReq = createSyncRequest(true);
		try{
			ReqAuthenticate(f,syncReq.requestId);
			LinkedList list = waitForAllRsps(syncReq);
			return (CThostFtdcRspAuthenticateField[])list.toArray(new CThostFtdcRspAuthenticateField[list.size()]);
		}finally{ removeSyncRequest(syncReq); }
	}

	/** 用户登录请求 */
	public int ReqUserLogin(CThostFtdcReqUserLoginField f) throws JctpException {
		int reqId = getNextRequestId();
		ReqUserLogin(f,reqId);
		return reqId;
	}
	/** 用户登录请求 */
	public void ReqUserLogin(CThostFtdcReqUserLoginField f, int nRequestID) throws JctpException {
		checkConnected();
		byte[] a1 = CThostFtdcReqUserLoginField.toBytes(f);
		int r = JctpApi.traderapiReq(this,nativeApiPtr, MethodId_TraderApi_ReqUserLogin, a1, nRequestID);
		if ( r!=0 ) throw new JctpException(r);
	}
	/** 用户登录请求(SYNC) */
	public CThostFtdcRspUserLoginField SyncReqUserLogin(CThostFtdcReqUserLoginField f) throws JctpException {
		SyncRequest syncReq = createSyncRequest(false);
		try{
			ReqUserLogin(f,syncReq.requestId);
			return (CThostFtdcRspUserLoginField)waitForRsp(syncReq);
		}finally{ removeSyncRequest(syncReq); }
	}
	/** 用户登录请求(SYNC ALL) */
	public CThostFtdcRspUserLoginField[] SyncAllReqUserLogin(CThostFtdcReqUserLoginField f) throws JctpException {
		SyncRequest syncReq = createSyncRequest(true);
		try{
			ReqUserLogin(f,syncReq.requestId);
			LinkedList list = waitForAllRsps(syncReq);
			return (CThostFtdcRspUserLoginField[])list.toArray(new CThostFtdcRspUserLoginField[list.size()]);
		}finally{ removeSyncRequest(syncReq); }
	}

	/** 登出请求 */
	public int ReqUserLogout(CThostFtdcUserLogoutField f) throws JctpException {
		int reqId = getNextRequestId();
		ReqUserLogout(f,reqId);
		return reqId;
	}
	/** 登出请求 */
	public void ReqUserLogout(CThostFtdcUserLogoutField f, int nRequestID) throws JctpException {
		checkConnected();
		byte[] a1 = CThostFtdcUserLogoutField.toBytes(f);
		int r = JctpApi.traderapiReq(this,nativeApiPtr, MethodId_TraderApi_ReqUserLogout, a1, nRequestID);
		if ( r!=0 ) throw new JctpException(r);
	}
	/** 登出请求(SYNC) */
	public CThostFtdcUserLogoutField SyncReqUserLogout(CThostFtdcUserLogoutField f) throws JctpException {
		SyncRequest syncReq = createSyncRequest(false);
		try{
			ReqUserLogout(f,syncReq.requestId);
			return (CThostFtdcUserLogoutField)waitForRsp(syncReq);
		}finally{ removeSyncRequest(syncReq); }
	}
	/** 登出请求(SYNC ALL) */
	public CThostFtdcUserLogoutField[] SyncAllReqUserLogout(CThostFtdcUserLogoutField f) throws JctpException {
		SyncRequest syncReq = createSyncRequest(true);
		try{
			ReqUserLogout(f,syncReq.requestId);
			LinkedList list = waitForAllRsps(syncReq);
			return (CThostFtdcUserLogoutField[])list.toArray(new CThostFtdcUserLogoutField[list.size()]);
		}finally{ removeSyncRequest(syncReq); }
	}

	/** 用户口令更新请求 */
	public int ReqUserPasswordUpdate(CThostFtdcUserPasswordUpdateField f) throws JctpException {
		int reqId = getNextRequestId();
		ReqUserPasswordUpdate(f,reqId);
		return reqId;
	}
	/** 用户口令更新请求 */
	public void ReqUserPasswordUpdate(CThostFtdcUserPasswordUpdateField f, int nRequestID) throws JctpException {
		checkConnected();
		byte[] a1 = CThostFtdcUserPasswordUpdateField.toBytes(f);
		int r = JctpApi.traderapiReq(this,nativeApiPtr, MethodId_TraderApi_ReqUserPasswordUpdate, a1, nRequestID);
		if ( r!=0 ) throw new JctpException(r);
	}
	/** 用户口令更新请求(SYNC) */
	public CThostFtdcUserPasswordUpdateField SyncReqUserPasswordUpdate(CThostFtdcUserPasswordUpdateField f) throws JctpException {
		SyncRequest syncReq = createSyncRequest(false);
		try{
			ReqUserPasswordUpdate(f,syncReq.requestId);
			return (CThostFtdcUserPasswordUpdateField)waitForRsp(syncReq);
		}finally{ removeSyncRequest(syncReq); }
	}
	/** 用户口令更新请求(SYNC ALL) */
	public CThostFtdcUserPasswordUpdateField[] SyncAllReqUserPasswordUpdate(CThostFtdcUserPasswordUpdateField f) throws JctpException {
		SyncRequest syncReq = createSyncRequest(true);
		try{
			ReqUserPasswordUpdate(f,syncReq.requestId);
			LinkedList list = waitForAllRsps(syncReq);
			return (CThostFtdcUserPasswordUpdateField[])list.toArray(new CThostFtdcUserPasswordUpdateField[list.size()]);
		}finally{ removeSyncRequest(syncReq); }
	}

	/** 资金账户口令更新请求 */
	public int ReqTradingAccountPasswordUpdate(CThostFtdcTradingAccountPasswordUpdateField f) throws JctpException {
		int reqId = getNextRequestId();
		ReqTradingAccountPasswordUpdate(f,reqId);
		return reqId;
	}
	/** 资金账户口令更新请求 */
	public void ReqTradingAccountPasswordUpdate(CThostFtdcTradingAccountPasswordUpdateField f, int nRequestID) throws JctpException {
		checkConnected();
		byte[] a1 = CThostFtdcTradingAccountPasswordUpdateField.toBytes(f);
		int r = JctpApi.traderapiReq(this,nativeApiPtr, MethodId_TraderApi_ReqTradingAccountPasswordUpdate, a1, nRequestID);
		if ( r!=0 ) throw new JctpException(r);
	}
	/** 资金账户口令更新请求(SYNC) */
	public CThostFtdcTradingAccountPasswordUpdateField SyncReqTradingAccountPasswordUpdate(CThostFtdcTradingAccountPasswordUpdateField f) throws JctpException {
		SyncRequest syncReq = createSyncRequest(false);
		try{
			ReqTradingAccountPasswordUpdate(f,syncReq.requestId);
			return (CThostFtdcTradingAccountPasswordUpdateField)waitForRsp(syncReq);
		}finally{ removeSyncRequest(syncReq); }
	}
	/** 资金账户口令更新请求(SYNC ALL) */
	public CThostFtdcTradingAccountPasswordUpdateField[] SyncAllReqTradingAccountPasswordUpdate(CThostFtdcTradingAccountPasswordUpdateField f) throws JctpException {
		SyncRequest syncReq = createSyncRequest(true);
		try{
			ReqTradingAccountPasswordUpdate(f,syncReq.requestId);
			LinkedList list = waitForAllRsps(syncReq);
			return (CThostFtdcTradingAccountPasswordUpdateField[])list.toArray(new CThostFtdcTradingAccountPasswordUpdateField[list.size()]);
		}finally{ removeSyncRequest(syncReq); }
	}

	/** 报单录入请求 */
	public int ReqOrderInsert(CThostFtdcInputOrderField f) throws JctpException {
		int reqId = getNextRequestId();
		ReqOrderInsert(f,reqId);
		return reqId;
	}
	/** 报单录入请求 */
	public void ReqOrderInsert(CThostFtdcInputOrderField f, int nRequestID) throws JctpException {
		checkConnected();
		byte[] a1 = CThostFtdcInputOrderField.toBytes(f);
		int r = JctpApi.traderapiReq(this,nativeApiPtr, MethodId_TraderApi_ReqOrderInsert, a1, nRequestID);
		if ( r!=0 ) throw new JctpException(r);
	}
	/** 报单录入请求(SYNC) */
	public CThostFtdcInputOrderField SyncReqOrderInsert(CThostFtdcInputOrderField f) throws JctpException {
		SyncRequest syncReq = createSyncRequest(false);
		try{
			ReqOrderInsert(f,syncReq.requestId);
			return (CThostFtdcInputOrderField)waitForRsp(syncReq);
		}finally{ removeSyncRequest(syncReq); }
	}
	/** 报单录入请求(SYNC ALL) */
	public CThostFtdcInputOrderField[] SyncAllReqOrderInsert(CThostFtdcInputOrderField f) throws JctpException {
		SyncRequest syncReq = createSyncRequest(true);
		try{
			ReqOrderInsert(f,syncReq.requestId);
			LinkedList list = waitForAllRsps(syncReq);
			return (CThostFtdcInputOrderField[])list.toArray(new CThostFtdcInputOrderField[list.size()]);
		}finally{ removeSyncRequest(syncReq); }
	}

	/** 预埋单录入请求 */
	public int ReqParkedOrderInsert(CThostFtdcParkedOrderField f) throws JctpException {
		int reqId = getNextRequestId();
		ReqParkedOrderInsert(f,reqId);
		return reqId;
	}
	/** 预埋单录入请求 */
	public void ReqParkedOrderInsert(CThostFtdcParkedOrderField f, int nRequestID) throws JctpException {
		checkConnected();
		byte[] a1 = CThostFtdcParkedOrderField.toBytes(f);
		int r = JctpApi.traderapiReq(this,nativeApiPtr, MethodId_TraderApi_ReqParkedOrderInsert, a1, nRequestID);
		if ( r!=0 ) throw new JctpException(r);
	}
	/** 预埋单录入请求(SYNC) */
	public CThostFtdcParkedOrderField SyncReqParkedOrderInsert(CThostFtdcParkedOrderField f) throws JctpException {
		SyncRequest syncReq = createSyncRequest(false);
		try{
			ReqParkedOrderInsert(f,syncReq.requestId);
			return (CThostFtdcParkedOrderField)waitForRsp(syncReq);
		}finally{ removeSyncRequest(syncReq); }
	}
	/** 预埋单录入请求(SYNC ALL) */
	public CThostFtdcParkedOrderField[] SyncAllReqParkedOrderInsert(CThostFtdcParkedOrderField f) throws JctpException {
		SyncRequest syncReq = createSyncRequest(true);
		try{
			ReqParkedOrderInsert(f,syncReq.requestId);
			LinkedList list = waitForAllRsps(syncReq);
			return (CThostFtdcParkedOrderField[])list.toArray(new CThostFtdcParkedOrderField[list.size()]);
		}finally{ removeSyncRequest(syncReq); }
	}

	/** 预埋撤单录入请求 */
	public int ReqParkedOrderAction(CThostFtdcParkedOrderActionField f) throws JctpException {
		int reqId = getNextRequestId();
		ReqParkedOrderAction(f,reqId);
		return reqId;
	}
	/** 预埋撤单录入请求 */
	public void ReqParkedOrderAction(CThostFtdcParkedOrderActionField f, int nRequestID) throws JctpException {
		checkConnected();
		byte[] a1 = CThostFtdcParkedOrderActionField.toBytes(f);
		int r = JctpApi.traderapiReq(this,nativeApiPtr, MethodId_TraderApi_ReqParkedOrderAction, a1, nRequestID);
		if ( r!=0 ) throw new JctpException(r);
	}
	/** 预埋撤单录入请求(SYNC) */
	public CThostFtdcParkedOrderActionField SyncReqParkedOrderAction(CThostFtdcParkedOrderActionField f) throws JctpException {
		SyncRequest syncReq = createSyncRequest(false);
		try{
			ReqParkedOrderAction(f,syncReq.requestId);
			return (CThostFtdcParkedOrderActionField)waitForRsp(syncReq);
		}finally{ removeSyncRequest(syncReq); }
	}
	/** 预埋撤单录入请求(SYNC ALL) */
	public CThostFtdcParkedOrderActionField[] SyncAllReqParkedOrderAction(CThostFtdcParkedOrderActionField f) throws JctpException {
		SyncRequest syncReq = createSyncRequest(true);
		try{
			ReqParkedOrderAction(f,syncReq.requestId);
			LinkedList list = waitForAllRsps(syncReq);
			return (CThostFtdcParkedOrderActionField[])list.toArray(new CThostFtdcParkedOrderActionField[list.size()]);
		}finally{ removeSyncRequest(syncReq); }
	}

	/** 报单操作请求 */
	public int ReqOrderAction(CThostFtdcInputOrderActionField f) throws JctpException {
		int reqId = getNextRequestId();
		ReqOrderAction(f,reqId);
		return reqId;
	}
	/** 报单操作请求 */
	public void ReqOrderAction(CThostFtdcInputOrderActionField f, int nRequestID) throws JctpException {
		checkConnected();
		byte[] a1 = CThostFtdcInputOrderActionField.toBytes(f);
		int r = JctpApi.traderapiReq(this,nativeApiPtr, MethodId_TraderApi_ReqOrderAction, a1, nRequestID);
		if ( r!=0 ) throw new JctpException(r);
	}
	/** 报单操作请求(SYNC) */
	public CThostFtdcInputOrderActionField SyncReqOrderAction(CThostFtdcInputOrderActionField f) throws JctpException {
		SyncRequest syncReq = createSyncRequest(false);
		try{
			ReqOrderAction(f,syncReq.requestId);
			return (CThostFtdcInputOrderActionField)waitForRsp(syncReq);
		}finally{ removeSyncRequest(syncReq); }
	}
	/** 报单操作请求(SYNC ALL) */
	public CThostFtdcInputOrderActionField[] SyncAllReqOrderAction(CThostFtdcInputOrderActionField f) throws JctpException {
		SyncRequest syncReq = createSyncRequest(true);
		try{
			ReqOrderAction(f,syncReq.requestId);
			LinkedList list = waitForAllRsps(syncReq);
			return (CThostFtdcInputOrderActionField[])list.toArray(new CThostFtdcInputOrderActionField[list.size()]);
		}finally{ removeSyncRequest(syncReq); }
	}

	/** 查询最大报单数量请求 */
	public int ReqQueryMaxOrderVolume(CThostFtdcQueryMaxOrderVolumeField f) throws JctpException {
		int reqId = getNextRequestId();
		ReqQueryMaxOrderVolume(f,reqId);
		return reqId;
	}
	/** 查询最大报单数量请求 */
	public void ReqQueryMaxOrderVolume(CThostFtdcQueryMaxOrderVolumeField f, int nRequestID) throws JctpException {
		checkConnected();
		byte[] a1 = CThostFtdcQueryMaxOrderVolumeField.toBytes(f);
		int r = JctpApi.traderapiReq(this,nativeApiPtr, MethodId_TraderApi_ReqQueryMaxOrderVolume, a1, nRequestID);
		if ( r!=0 ) throw new JctpException(r);
	}
	/** 查询最大报单数量请求(SYNC) */
	public CThostFtdcQueryMaxOrderVolumeField SyncReqQueryMaxOrderVolume(CThostFtdcQueryMaxOrderVolumeField f) throws JctpException {
		SyncRequest syncReq = createSyncRequest(false);
		try{
			ReqQueryMaxOrderVolume(f,syncReq.requestId);
			return (CThostFtdcQueryMaxOrderVolumeField)waitForRsp(syncReq);
		}finally{ removeSyncRequest(syncReq); }
	}
	/** 查询最大报单数量请求(SYNC ALL) */
	public CThostFtdcQueryMaxOrderVolumeField[] SyncAllReqQueryMaxOrderVolume(CThostFtdcQueryMaxOrderVolumeField f) throws JctpException {
		SyncRequest syncReq = createSyncRequest(true);
		try{
			ReqQueryMaxOrderVolume(f,syncReq.requestId);
			LinkedList list = waitForAllRsps(syncReq);
			return (CThostFtdcQueryMaxOrderVolumeField[])list.toArray(new CThostFtdcQueryMaxOrderVolumeField[list.size()]);
		}finally{ removeSyncRequest(syncReq); }
	}

	/** 投资者结算结果确认 */
	public int ReqSettlementInfoConfirm(CThostFtdcSettlementInfoConfirmField f) throws JctpException {
		int reqId = getNextRequestId();
		ReqSettlementInfoConfirm(f,reqId);
		return reqId;
	}
	/** 投资者结算结果确认 */
	public void ReqSettlementInfoConfirm(CThostFtdcSettlementInfoConfirmField f, int nRequestID) throws JctpException {
		checkConnected();
		byte[] a1 = CThostFtdcSettlementInfoConfirmField.toBytes(f);
		int r = JctpApi.traderapiReq(this,nativeApiPtr, MethodId_TraderApi_ReqSettlementInfoConfirm, a1, nRequestID);
		if ( r!=0 ) throw new JctpException(r);
	}
	/** 投资者结算结果确认(SYNC) */
	public CThostFtdcSettlementInfoConfirmField SyncReqSettlementInfoConfirm(CThostFtdcSettlementInfoConfirmField f) throws JctpException {
		SyncRequest syncReq = createSyncRequest(false);
		try{
			ReqSettlementInfoConfirm(f,syncReq.requestId);
			return (CThostFtdcSettlementInfoConfirmField)waitForRsp(syncReq);
		}finally{ removeSyncRequest(syncReq); }
	}
	/** 投资者结算结果确认(SYNC ALL) */
	public CThostFtdcSettlementInfoConfirmField[] SyncAllReqSettlementInfoConfirm(CThostFtdcSettlementInfoConfirmField f) throws JctpException {
		SyncRequest syncReq = createSyncRequest(true);
		try{
			ReqSettlementInfoConfirm(f,syncReq.requestId);
			LinkedList list = waitForAllRsps(syncReq);
			return (CThostFtdcSettlementInfoConfirmField[])list.toArray(new CThostFtdcSettlementInfoConfirmField[list.size()]);
		}finally{ removeSyncRequest(syncReq); }
	}

	/** 请求删除预埋单 */
	public int ReqRemoveParkedOrder(CThostFtdcRemoveParkedOrderField f) throws JctpException {
		int reqId = getNextRequestId();
		ReqRemoveParkedOrder(f,reqId);
		return reqId;
	}
	/** 请求删除预埋单 */
	public void ReqRemoveParkedOrder(CThostFtdcRemoveParkedOrderField f, int nRequestID) throws JctpException {
		checkConnected();
		byte[] a1 = CThostFtdcRemoveParkedOrderField.toBytes(f);
		int r = JctpApi.traderapiReq(this,nativeApiPtr, MethodId_TraderApi_ReqRemoveParkedOrder, a1, nRequestID);
		if ( r!=0 ) throw new JctpException(r);
	}
	/** 请求删除预埋单(SYNC) */
	public CThostFtdcRemoveParkedOrderField SyncReqRemoveParkedOrder(CThostFtdcRemoveParkedOrderField f) throws JctpException {
		SyncRequest syncReq = createSyncRequest(false);
		try{
			ReqRemoveParkedOrder(f,syncReq.requestId);
			return (CThostFtdcRemoveParkedOrderField)waitForRsp(syncReq);
		}finally{ removeSyncRequest(syncReq); }
	}
	/** 请求删除预埋单(SYNC ALL) */
	public CThostFtdcRemoveParkedOrderField[] SyncAllReqRemoveParkedOrder(CThostFtdcRemoveParkedOrderField f) throws JctpException {
		SyncRequest syncReq = createSyncRequest(true);
		try{
			ReqRemoveParkedOrder(f,syncReq.requestId);
			LinkedList list = waitForAllRsps(syncReq);
			return (CThostFtdcRemoveParkedOrderField[])list.toArray(new CThostFtdcRemoveParkedOrderField[list.size()]);
		}finally{ removeSyncRequest(syncReq); }
	}

	/** 请求删除预埋撤单 */
	public int ReqRemoveParkedOrderAction(CThostFtdcRemoveParkedOrderActionField f) throws JctpException {
		int reqId = getNextRequestId();
		ReqRemoveParkedOrderAction(f,reqId);
		return reqId;
	}
	/** 请求删除预埋撤单 */
	public void ReqRemoveParkedOrderAction(CThostFtdcRemoveParkedOrderActionField f, int nRequestID) throws JctpException {
		checkConnected();
		byte[] a1 = CThostFtdcRemoveParkedOrderActionField.toBytes(f);
		int r = JctpApi.traderapiReq(this,nativeApiPtr, MethodId_TraderApi_ReqRemoveParkedOrderAction, a1, nRequestID);
		if ( r!=0 ) throw new JctpException(r);
	}
	/** 请求删除预埋撤单(SYNC) */
	public CThostFtdcRemoveParkedOrderActionField SyncReqRemoveParkedOrderAction(CThostFtdcRemoveParkedOrderActionField f) throws JctpException {
		SyncRequest syncReq = createSyncRequest(false);
		try{
			ReqRemoveParkedOrderAction(f,syncReq.requestId);
			return (CThostFtdcRemoveParkedOrderActionField)waitForRsp(syncReq);
		}finally{ removeSyncRequest(syncReq); }
	}
	/** 请求删除预埋撤单(SYNC ALL) */
	public CThostFtdcRemoveParkedOrderActionField[] SyncAllReqRemoveParkedOrderAction(CThostFtdcRemoveParkedOrderActionField f) throws JctpException {
		SyncRequest syncReq = createSyncRequest(true);
		try{
			ReqRemoveParkedOrderAction(f,syncReq.requestId);
			LinkedList list = waitForAllRsps(syncReq);
			return (CThostFtdcRemoveParkedOrderActionField[])list.toArray(new CThostFtdcRemoveParkedOrderActionField[list.size()]);
		}finally{ removeSyncRequest(syncReq); }
	}

	/** 执行宣告录入请求 */
	public int ReqExecOrderInsert(CThostFtdcInputExecOrderField f) throws JctpException {
		int reqId = getNextRequestId();
		ReqExecOrderInsert(f,reqId);
		return reqId;
	}
	/** 执行宣告录入请求 */
	public void ReqExecOrderInsert(CThostFtdcInputExecOrderField f, int nRequestID) throws JctpException {
		checkConnected();
		byte[] a1 = CThostFtdcInputExecOrderField.toBytes(f);
		int r = JctpApi.traderapiReq(this,nativeApiPtr, MethodId_TraderApi_ReqExecOrderInsert, a1, nRequestID);
		if ( r!=0 ) throw new JctpException(r);
	}
	/** 执行宣告录入请求(SYNC) */
	public CThostFtdcInputExecOrderField SyncReqExecOrderInsert(CThostFtdcInputExecOrderField f) throws JctpException {
		SyncRequest syncReq = createSyncRequest(false);
		try{
			ReqExecOrderInsert(f,syncReq.requestId);
			return (CThostFtdcInputExecOrderField)waitForRsp(syncReq);
		}finally{ removeSyncRequest(syncReq); }
	}
	/** 执行宣告录入请求(SYNC ALL) */
	public CThostFtdcInputExecOrderField[] SyncAllReqExecOrderInsert(CThostFtdcInputExecOrderField f) throws JctpException {
		SyncRequest syncReq = createSyncRequest(true);
		try{
			ReqExecOrderInsert(f,syncReq.requestId);
			LinkedList list = waitForAllRsps(syncReq);
			return (CThostFtdcInputExecOrderField[])list.toArray(new CThostFtdcInputExecOrderField[list.size()]);
		}finally{ removeSyncRequest(syncReq); }
	}

	/** 执行宣告操作请求 */
	public int ReqExecOrderAction(CThostFtdcInputExecOrderActionField f) throws JctpException {
		int reqId = getNextRequestId();
		ReqExecOrderAction(f,reqId);
		return reqId;
	}
	/** 执行宣告操作请求 */
	public void ReqExecOrderAction(CThostFtdcInputExecOrderActionField f, int nRequestID) throws JctpException {
		checkConnected();
		byte[] a1 = CThostFtdcInputExecOrderActionField.toBytes(f);
		int r = JctpApi.traderapiReq(this,nativeApiPtr, MethodId_TraderApi_ReqExecOrderAction, a1, nRequestID);
		if ( r!=0 ) throw new JctpException(r);
	}
	/** 执行宣告操作请求(SYNC) */
	public CThostFtdcInputExecOrderActionField SyncReqExecOrderAction(CThostFtdcInputExecOrderActionField f) throws JctpException {
		SyncRequest syncReq = createSyncRequest(false);
		try{
			ReqExecOrderAction(f,syncReq.requestId);
			return (CThostFtdcInputExecOrderActionField)waitForRsp(syncReq);
		}finally{ removeSyncRequest(syncReq); }
	}
	/** 执行宣告操作请求(SYNC ALL) */
	public CThostFtdcInputExecOrderActionField[] SyncAllReqExecOrderAction(CThostFtdcInputExecOrderActionField f) throws JctpException {
		SyncRequest syncReq = createSyncRequest(true);
		try{
			ReqExecOrderAction(f,syncReq.requestId);
			LinkedList list = waitForAllRsps(syncReq);
			return (CThostFtdcInputExecOrderActionField[])list.toArray(new CThostFtdcInputExecOrderActionField[list.size()]);
		}finally{ removeSyncRequest(syncReq); }
	}

	/** 询价录入请求 */
	public int ReqForQuoteInsert(CThostFtdcInputForQuoteField f) throws JctpException {
		int reqId = getNextRequestId();
		ReqForQuoteInsert(f,reqId);
		return reqId;
	}
	/** 询价录入请求 */
	public void ReqForQuoteInsert(CThostFtdcInputForQuoteField f, int nRequestID) throws JctpException {
		checkConnected();
		byte[] a1 = CThostFtdcInputForQuoteField.toBytes(f);
		int r = JctpApi.traderapiReq(this,nativeApiPtr, MethodId_TraderApi_ReqForQuoteInsert, a1, nRequestID);
		if ( r!=0 ) throw new JctpException(r);
	}
	/** 询价录入请求(SYNC) */
	public CThostFtdcInputForQuoteField SyncReqForQuoteInsert(CThostFtdcInputForQuoteField f) throws JctpException {
		SyncRequest syncReq = createSyncRequest(false);
		try{
			ReqForQuoteInsert(f,syncReq.requestId);
			return (CThostFtdcInputForQuoteField)waitForRsp(syncReq);
		}finally{ removeSyncRequest(syncReq); }
	}
	/** 询价录入请求(SYNC ALL) */
	public CThostFtdcInputForQuoteField[] SyncAllReqForQuoteInsert(CThostFtdcInputForQuoteField f) throws JctpException {
		SyncRequest syncReq = createSyncRequest(true);
		try{
			ReqForQuoteInsert(f,syncReq.requestId);
			LinkedList list = waitForAllRsps(syncReq);
			return (CThostFtdcInputForQuoteField[])list.toArray(new CThostFtdcInputForQuoteField[list.size()]);
		}finally{ removeSyncRequest(syncReq); }
	}

	/** 报价录入请求 */
	public int ReqQuoteInsert(CThostFtdcInputQuoteField f) throws JctpException {
		int reqId = getNextRequestId();
		ReqQuoteInsert(f,reqId);
		return reqId;
	}
	/** 报价录入请求 */
	public void ReqQuoteInsert(CThostFtdcInputQuoteField f, int nRequestID) throws JctpException {
		checkConnected();
		byte[] a1 = CThostFtdcInputQuoteField.toBytes(f);
		int r = JctpApi.traderapiReq(this,nativeApiPtr, MethodId_TraderApi_ReqQuoteInsert, a1, nRequestID);
		if ( r!=0 ) throw new JctpException(r);
	}
	/** 报价录入请求(SYNC) */
	public CThostFtdcInputQuoteField SyncReqQuoteInsert(CThostFtdcInputQuoteField f) throws JctpException {
		SyncRequest syncReq = createSyncRequest(false);
		try{
			ReqQuoteInsert(f,syncReq.requestId);
			return (CThostFtdcInputQuoteField)waitForRsp(syncReq);
		}finally{ removeSyncRequest(syncReq); }
	}
	/** 报价录入请求(SYNC ALL) */
	public CThostFtdcInputQuoteField[] SyncAllReqQuoteInsert(CThostFtdcInputQuoteField f) throws JctpException {
		SyncRequest syncReq = createSyncRequest(true);
		try{
			ReqQuoteInsert(f,syncReq.requestId);
			LinkedList list = waitForAllRsps(syncReq);
			return (CThostFtdcInputQuoteField[])list.toArray(new CThostFtdcInputQuoteField[list.size()]);
		}finally{ removeSyncRequest(syncReq); }
	}

	/** 报价操作请求 */
	public int ReqQuoteAction(CThostFtdcInputQuoteActionField f) throws JctpException {
		int reqId = getNextRequestId();
		ReqQuoteAction(f,reqId);
		return reqId;
	}
	/** 报价操作请求 */
	public void ReqQuoteAction(CThostFtdcInputQuoteActionField f, int nRequestID) throws JctpException {
		checkConnected();
		byte[] a1 = CThostFtdcInputQuoteActionField.toBytes(f);
		int r = JctpApi.traderapiReq(this,nativeApiPtr, MethodId_TraderApi_ReqQuoteAction, a1, nRequestID);
		if ( r!=0 ) throw new JctpException(r);
	}
	/** 报价操作请求(SYNC) */
	public CThostFtdcInputQuoteActionField SyncReqQuoteAction(CThostFtdcInputQuoteActionField f) throws JctpException {
		SyncRequest syncReq = createSyncRequest(false);
		try{
			ReqQuoteAction(f,syncReq.requestId);
			return (CThostFtdcInputQuoteActionField)waitForRsp(syncReq);
		}finally{ removeSyncRequest(syncReq); }
	}
	/** 报价操作请求(SYNC ALL) */
	public CThostFtdcInputQuoteActionField[] SyncAllReqQuoteAction(CThostFtdcInputQuoteActionField f) throws JctpException {
		SyncRequest syncReq = createSyncRequest(true);
		try{
			ReqQuoteAction(f,syncReq.requestId);
			LinkedList list = waitForAllRsps(syncReq);
			return (CThostFtdcInputQuoteActionField[])list.toArray(new CThostFtdcInputQuoteActionField[list.size()]);
		}finally{ removeSyncRequest(syncReq); }
	}

	/** 请求查询报单 */
	public int ReqQryOrder(CThostFtdcQryOrderField f) throws JctpException {
		int reqId = getNextRequestId();
		ReqQryOrder(f,reqId);
		return reqId;
	}
	/** 请求查询报单 */
	public void ReqQryOrder(CThostFtdcQryOrderField f, int nRequestID) throws JctpException {
		checkConnected();
		autoSleepReqQry();
		byte[] a1 = CThostFtdcQryOrderField.toBytes(f);
		int r = JctpApi.traderapiReq(this,nativeApiPtr, MethodId_TraderApi_ReqQryOrder, a1, nRequestID);
		if ( r!=0 ) throw new JctpException(r);
	}
	/** 请求查询报单(SYNC) */
	public CThostFtdcOrderField SyncReqQryOrder(CThostFtdcQryOrderField f) throws JctpException {
		SyncRequest syncReq = createSyncRequest(false);
		try{
			ReqQryOrder(f,syncReq.requestId);
			return (CThostFtdcOrderField)waitForRsp(syncReq);
		}finally{ removeSyncRequest(syncReq); }
	}
	/** 请求查询报单(SYNC ALL) */
	public CThostFtdcOrderField[] SyncAllReqQryOrder(CThostFtdcQryOrderField f) throws JctpException {
		SyncRequest syncReq = createSyncRequest(true);
		try{
			ReqQryOrder(f,syncReq.requestId);
			LinkedList list = waitForAllRsps(syncReq);
			return (CThostFtdcOrderField[])list.toArray(new CThostFtdcOrderField[list.size()]);
		}finally{ removeSyncRequest(syncReq); }
	}

	/** 请求查询成交 */
	public int ReqQryTrade(CThostFtdcQryTradeField f) throws JctpException {
		int reqId = getNextRequestId();
		ReqQryTrade(f,reqId);
		return reqId;
	}
	/** 请求查询成交 */
	public void ReqQryTrade(CThostFtdcQryTradeField f, int nRequestID) throws JctpException {
		checkConnected();
		autoSleepReqQry();
		byte[] a1 = CThostFtdcQryTradeField.toBytes(f);
		int r = JctpApi.traderapiReq(this,nativeApiPtr, MethodId_TraderApi_ReqQryTrade, a1, nRequestID);
		if ( r!=0 ) throw new JctpException(r);
	}
	/** 请求查询成交(SYNC) */
	public CThostFtdcTradeField SyncReqQryTrade(CThostFtdcQryTradeField f) throws JctpException {
		SyncRequest syncReq = createSyncRequest(false);
		try{
			ReqQryTrade(f,syncReq.requestId);
			return (CThostFtdcTradeField)waitForRsp(syncReq);
		}finally{ removeSyncRequest(syncReq); }
	}
	/** 请求查询成交(SYNC ALL) */
	public CThostFtdcTradeField[] SyncAllReqQryTrade(CThostFtdcQryTradeField f) throws JctpException {
		SyncRequest syncReq = createSyncRequest(true);
		try{
			ReqQryTrade(f,syncReq.requestId);
			LinkedList list = waitForAllRsps(syncReq);
			return (CThostFtdcTradeField[])list.toArray(new CThostFtdcTradeField[list.size()]);
		}finally{ removeSyncRequest(syncReq); }
	}

	/** 请求查询投资者持仓 */
	public int ReqQryInvestorPosition(CThostFtdcQryInvestorPositionField f) throws JctpException {
		int reqId = getNextRequestId();
		ReqQryInvestorPosition(f,reqId);
		return reqId;
	}
	/** 请求查询投资者持仓 */
	public void ReqQryInvestorPosition(CThostFtdcQryInvestorPositionField f, int nRequestID) throws JctpException {
		checkConnected();
		autoSleepReqQry();
		byte[] a1 = CThostFtdcQryInvestorPositionField.toBytes(f);
		int r = JctpApi.traderapiReq(this,nativeApiPtr, MethodId_TraderApi_ReqQryInvestorPosition, a1, nRequestID);
		if ( r!=0 ) throw new JctpException(r);
	}
	/** 请求查询投资者持仓(SYNC) */
	public CThostFtdcInvestorPositionField SyncReqQryInvestorPosition(CThostFtdcQryInvestorPositionField f) throws JctpException {
		SyncRequest syncReq = createSyncRequest(false);
		try{
			ReqQryInvestorPosition(f,syncReq.requestId);
			return (CThostFtdcInvestorPositionField)waitForRsp(syncReq);
		}finally{ removeSyncRequest(syncReq); }
	}
	/** 请求查询投资者持仓(SYNC ALL) */
	public CThostFtdcInvestorPositionField[] SyncAllReqQryInvestorPosition(CThostFtdcQryInvestorPositionField f) throws JctpException {
		SyncRequest syncReq = createSyncRequest(true);
		try{
			ReqQryInvestorPosition(f,syncReq.requestId);
			LinkedList list = waitForAllRsps(syncReq);
			return (CThostFtdcInvestorPositionField[])list.toArray(new CThostFtdcInvestorPositionField[list.size()]);
		}finally{ removeSyncRequest(syncReq); }
	}

	/** 请求查询资金账户 */
	public int ReqQryTradingAccount(CThostFtdcQryTradingAccountField f) throws JctpException {
		int reqId = getNextRequestId();
		ReqQryTradingAccount(f,reqId);
		return reqId;
	}
	/** 请求查询资金账户 */
	public void ReqQryTradingAccount(CThostFtdcQryTradingAccountField f, int nRequestID) throws JctpException {
		checkConnected();
		autoSleepReqQry();
		byte[] a1 = CThostFtdcQryTradingAccountField.toBytes(f);
		int r = JctpApi.traderapiReq(this,nativeApiPtr, MethodId_TraderApi_ReqQryTradingAccount, a1, nRequestID);
		if ( r!=0 ) throw new JctpException(r);
	}
	/** 请求查询资金账户(SYNC) */
	public CThostFtdcTradingAccountField SyncReqQryTradingAccount(CThostFtdcQryTradingAccountField f) throws JctpException {
		SyncRequest syncReq = createSyncRequest(false);
		try{
			ReqQryTradingAccount(f,syncReq.requestId);
			return (CThostFtdcTradingAccountField)waitForRsp(syncReq);
		}finally{ removeSyncRequest(syncReq); }
	}
	/** 请求查询资金账户(SYNC ALL) */
	public CThostFtdcTradingAccountField[] SyncAllReqQryTradingAccount(CThostFtdcQryTradingAccountField f) throws JctpException {
		SyncRequest syncReq = createSyncRequest(true);
		try{
			ReqQryTradingAccount(f,syncReq.requestId);
			LinkedList list = waitForAllRsps(syncReq);
			return (CThostFtdcTradingAccountField[])list.toArray(new CThostFtdcTradingAccountField[list.size()]);
		}finally{ removeSyncRequest(syncReq); }
	}

	/** 请求查询投资者 */
	public int ReqQryInvestor(CThostFtdcQryInvestorField f) throws JctpException {
		int reqId = getNextRequestId();
		ReqQryInvestor(f,reqId);
		return reqId;
	}
	/** 请求查询投资者 */
	public void ReqQryInvestor(CThostFtdcQryInvestorField f, int nRequestID) throws JctpException {
		checkConnected();
		autoSleepReqQry();
		byte[] a1 = CThostFtdcQryInvestorField.toBytes(f);
		int r = JctpApi.traderapiReq(this,nativeApiPtr, MethodId_TraderApi_ReqQryInvestor, a1, nRequestID);
		if ( r!=0 ) throw new JctpException(r);
	}
	/** 请求查询投资者(SYNC) */
	public CThostFtdcInvestorField SyncReqQryInvestor(CThostFtdcQryInvestorField f) throws JctpException {
		SyncRequest syncReq = createSyncRequest(false);
		try{
			ReqQryInvestor(f,syncReq.requestId);
			return (CThostFtdcInvestorField)waitForRsp(syncReq);
		}finally{ removeSyncRequest(syncReq); }
	}
	/** 请求查询投资者(SYNC ALL) */
	public CThostFtdcInvestorField[] SyncAllReqQryInvestor(CThostFtdcQryInvestorField f) throws JctpException {
		SyncRequest syncReq = createSyncRequest(true);
		try{
			ReqQryInvestor(f,syncReq.requestId);
			LinkedList list = waitForAllRsps(syncReq);
			return (CThostFtdcInvestorField[])list.toArray(new CThostFtdcInvestorField[list.size()]);
		}finally{ removeSyncRequest(syncReq); }
	}

	/** 请求查询交易编码 */
	public int ReqQryTradingCode(CThostFtdcQryTradingCodeField f) throws JctpException {
		int reqId = getNextRequestId();
		ReqQryTradingCode(f,reqId);
		return reqId;
	}
	/** 请求查询交易编码 */
	public void ReqQryTradingCode(CThostFtdcQryTradingCodeField f, int nRequestID) throws JctpException {
		checkConnected();
		autoSleepReqQry();
		byte[] a1 = CThostFtdcQryTradingCodeField.toBytes(f);
		int r = JctpApi.traderapiReq(this,nativeApiPtr, MethodId_TraderApi_ReqQryTradingCode, a1, nRequestID);
		if ( r!=0 ) throw new JctpException(r);
	}
	/** 请求查询交易编码(SYNC) */
	public CThostFtdcTradingCodeField SyncReqQryTradingCode(CThostFtdcQryTradingCodeField f) throws JctpException {
		SyncRequest syncReq = createSyncRequest(false);
		try{
			ReqQryTradingCode(f,syncReq.requestId);
			return (CThostFtdcTradingCodeField)waitForRsp(syncReq);
		}finally{ removeSyncRequest(syncReq); }
	}
	/** 请求查询交易编码(SYNC ALL) */
	public CThostFtdcTradingCodeField[] SyncAllReqQryTradingCode(CThostFtdcQryTradingCodeField f) throws JctpException {
		SyncRequest syncReq = createSyncRequest(true);
		try{
			ReqQryTradingCode(f,syncReq.requestId);
			LinkedList list = waitForAllRsps(syncReq);
			return (CThostFtdcTradingCodeField[])list.toArray(new CThostFtdcTradingCodeField[list.size()]);
		}finally{ removeSyncRequest(syncReq); }
	}

	/** 请求查询合约保证金率 */
	public int ReqQryInstrumentMarginRate(CThostFtdcQryInstrumentMarginRateField f) throws JctpException {
		int reqId = getNextRequestId();
		ReqQryInstrumentMarginRate(f,reqId);
		return reqId;
	}
	/** 请求查询合约保证金率 */
	public void ReqQryInstrumentMarginRate(CThostFtdcQryInstrumentMarginRateField f, int nRequestID) throws JctpException {
		checkConnected();
		autoSleepReqQry();
		byte[] a1 = CThostFtdcQryInstrumentMarginRateField.toBytes(f);
		int r = JctpApi.traderapiReq(this,nativeApiPtr, MethodId_TraderApi_ReqQryInstrumentMarginRate, a1, nRequestID);
		if ( r!=0 ) throw new JctpException(r);
	}
	/** 请求查询合约保证金率(SYNC) */
	public CThostFtdcInstrumentMarginRateField SyncReqQryInstrumentMarginRate(CThostFtdcQryInstrumentMarginRateField f) throws JctpException {
		SyncRequest syncReq = createSyncRequest(false);
		try{
			ReqQryInstrumentMarginRate(f,syncReq.requestId);
			return (CThostFtdcInstrumentMarginRateField)waitForRsp(syncReq);
		}finally{ removeSyncRequest(syncReq); }
	}
	/** 请求查询合约保证金率(SYNC ALL) */
	public CThostFtdcInstrumentMarginRateField[] SyncAllReqQryInstrumentMarginRate(CThostFtdcQryInstrumentMarginRateField f) throws JctpException {
		SyncRequest syncReq = createSyncRequest(true);
		try{
			ReqQryInstrumentMarginRate(f,syncReq.requestId);
			LinkedList list = waitForAllRsps(syncReq);
			return (CThostFtdcInstrumentMarginRateField[])list.toArray(new CThostFtdcInstrumentMarginRateField[list.size()]);
		}finally{ removeSyncRequest(syncReq); }
	}

	/** 请求查询合约手续费率 */
	public int ReqQryInstrumentCommissionRate(CThostFtdcQryInstrumentCommissionRateField f) throws JctpException {
		int reqId = getNextRequestId();
		ReqQryInstrumentCommissionRate(f,reqId);
		return reqId;
	}
	/** 请求查询合约手续费率 */
	public void ReqQryInstrumentCommissionRate(CThostFtdcQryInstrumentCommissionRateField f, int nRequestID) throws JctpException {
		checkConnected();
		autoSleepReqQry();
		byte[] a1 = CThostFtdcQryInstrumentCommissionRateField.toBytes(f);
		int r = JctpApi.traderapiReq(this,nativeApiPtr, MethodId_TraderApi_ReqQryInstrumentCommissionRate, a1, nRequestID);
		if ( r!=0 ) throw new JctpException(r);
	}
	/** 请求查询合约手续费率(SYNC) */
	public CThostFtdcInstrumentCommissionRateField SyncReqQryInstrumentCommissionRate(CThostFtdcQryInstrumentCommissionRateField f) throws JctpException {
		SyncRequest syncReq = createSyncRequest(false);
		try{
			ReqQryInstrumentCommissionRate(f,syncReq.requestId);
			return (CThostFtdcInstrumentCommissionRateField)waitForRsp(syncReq);
		}finally{ removeSyncRequest(syncReq); }
	}
	/** 请求查询合约手续费率(SYNC ALL) */
	public CThostFtdcInstrumentCommissionRateField[] SyncAllReqQryInstrumentCommissionRate(CThostFtdcQryInstrumentCommissionRateField f) throws JctpException {
		SyncRequest syncReq = createSyncRequest(true);
		try{
			ReqQryInstrumentCommissionRate(f,syncReq.requestId);
			LinkedList list = waitForAllRsps(syncReq);
			return (CThostFtdcInstrumentCommissionRateField[])list.toArray(new CThostFtdcInstrumentCommissionRateField[list.size()]);
		}finally{ removeSyncRequest(syncReq); }
	}

	/** 请求查询交易所 */
	public int ReqQryExchange(CThostFtdcQryExchangeField f) throws JctpException {
		int reqId = getNextRequestId();
		ReqQryExchange(f,reqId);
		return reqId;
	}
	/** 请求查询交易所 */
	public void ReqQryExchange(CThostFtdcQryExchangeField f, int nRequestID) throws JctpException {
		checkConnected();
		autoSleepReqQry();
		byte[] a1 = CThostFtdcQryExchangeField.toBytes(f);
		int r = JctpApi.traderapiReq(this,nativeApiPtr, MethodId_TraderApi_ReqQryExchange, a1, nRequestID);
		if ( r!=0 ) throw new JctpException(r);
	}
	/** 请求查询交易所(SYNC) */
	public CThostFtdcExchangeField SyncReqQryExchange(CThostFtdcQryExchangeField f) throws JctpException {
		SyncRequest syncReq = createSyncRequest(false);
		try{
			ReqQryExchange(f,syncReq.requestId);
			return (CThostFtdcExchangeField)waitForRsp(syncReq);
		}finally{ removeSyncRequest(syncReq); }
	}
	/** 请求查询交易所(SYNC ALL) */
	public CThostFtdcExchangeField[] SyncAllReqQryExchange(CThostFtdcQryExchangeField f) throws JctpException {
		SyncRequest syncReq = createSyncRequest(true);
		try{
			ReqQryExchange(f,syncReq.requestId);
			LinkedList list = waitForAllRsps(syncReq);
			return (CThostFtdcExchangeField[])list.toArray(new CThostFtdcExchangeField[list.size()]);
		}finally{ removeSyncRequest(syncReq); }
	}

	/** 请求查询产品 */
	public int ReqQryProduct(CThostFtdcQryProductField f) throws JctpException {
		int reqId = getNextRequestId();
		ReqQryProduct(f,reqId);
		return reqId;
	}
	/** 请求查询产品 */
	public void ReqQryProduct(CThostFtdcQryProductField f, int nRequestID) throws JctpException {
		checkConnected();
		autoSleepReqQry();
		byte[] a1 = CThostFtdcQryProductField.toBytes(f);
		int r = JctpApi.traderapiReq(this,nativeApiPtr, MethodId_TraderApi_ReqQryProduct, a1, nRequestID);
		if ( r!=0 ) throw new JctpException(r);
	}
	/** 请求查询产品(SYNC) */
	public CThostFtdcProductField SyncReqQryProduct(CThostFtdcQryProductField f) throws JctpException {
		SyncRequest syncReq = createSyncRequest(false);
		try{
			ReqQryProduct(f,syncReq.requestId);
			return (CThostFtdcProductField)waitForRsp(syncReq);
		}finally{ removeSyncRequest(syncReq); }
	}
	/** 请求查询产品(SYNC ALL) */
	public CThostFtdcProductField[] SyncAllReqQryProduct(CThostFtdcQryProductField f) throws JctpException {
		SyncRequest syncReq = createSyncRequest(true);
		try{
			ReqQryProduct(f,syncReq.requestId);
			LinkedList list = waitForAllRsps(syncReq);
			return (CThostFtdcProductField[])list.toArray(new CThostFtdcProductField[list.size()]);
		}finally{ removeSyncRequest(syncReq); }
	}

	/** 请求查询合约 */
	public int ReqQryInstrument(CThostFtdcQryInstrumentField f) throws JctpException {
		int reqId = getNextRequestId();
		ReqQryInstrument(f,reqId);
		return reqId;
	}
	/** 请求查询合约 */
	public void ReqQryInstrument(CThostFtdcQryInstrumentField f, int nRequestID) throws JctpException {
		checkConnected();
		autoSleepReqQry();
		byte[] a1 = CThostFtdcQryInstrumentField.toBytes(f);
		int r = JctpApi.traderapiReq(this,nativeApiPtr, MethodId_TraderApi_ReqQryInstrument, a1, nRequestID);
		if ( r!=0 ) throw new JctpException(r);
	}
	/** 请求查询合约(SYNC) */
	public CThostFtdcInstrumentField SyncReqQryInstrument(CThostFtdcQryInstrumentField f) throws JctpException {
		SyncRequest syncReq = createSyncRequest(false);
		try{
			ReqQryInstrument(f,syncReq.requestId);
			return (CThostFtdcInstrumentField)waitForRsp(syncReq);
		}finally{ removeSyncRequest(syncReq); }
	}
	/** 请求查询合约(SYNC ALL) */
	public CThostFtdcInstrumentField[] SyncAllReqQryInstrument(CThostFtdcQryInstrumentField f) throws JctpException {
		SyncRequest syncReq = createSyncRequest(true);
		try{
			ReqQryInstrument(f,syncReq.requestId);
			LinkedList list = waitForAllRsps(syncReq);
			return (CThostFtdcInstrumentField[])list.toArray(new CThostFtdcInstrumentField[list.size()]);
		}finally{ removeSyncRequest(syncReq); }
	}

	/** 请求查询行情 */
	public int ReqQryDepthMarketData(CThostFtdcQryDepthMarketDataField f) throws JctpException {
		int reqId = getNextRequestId();
		ReqQryDepthMarketData(f,reqId);
		return reqId;
	}
	/** 请求查询行情 */
	public void ReqQryDepthMarketData(CThostFtdcQryDepthMarketDataField f, int nRequestID) throws JctpException {
		checkConnected();
		autoSleepReqQry();
		byte[] a1 = CThostFtdcQryDepthMarketDataField.toBytes(f);
		int r = JctpApi.traderapiReq(this,nativeApiPtr, MethodId_TraderApi_ReqQryDepthMarketData, a1, nRequestID);
		if ( r!=0 ) throw new JctpException(r);
	}
	/** 请求查询行情(SYNC) */
	public CThostFtdcDepthMarketDataField SyncReqQryDepthMarketData(CThostFtdcQryDepthMarketDataField f) throws JctpException {
		SyncRequest syncReq = createSyncRequest(false);
		try{
			ReqQryDepthMarketData(f,syncReq.requestId);
			return (CThostFtdcDepthMarketDataField)waitForRsp(syncReq);
		}finally{ removeSyncRequest(syncReq); }
	}
	/** 请求查询行情(SYNC ALL) */
	public CThostFtdcDepthMarketDataField[] SyncAllReqQryDepthMarketData(CThostFtdcQryDepthMarketDataField f) throws JctpException {
		SyncRequest syncReq = createSyncRequest(true);
		try{
			ReqQryDepthMarketData(f,syncReq.requestId);
			LinkedList list = waitForAllRsps(syncReq);
			return (CThostFtdcDepthMarketDataField[])list.toArray(new CThostFtdcDepthMarketDataField[list.size()]);
		}finally{ removeSyncRequest(syncReq); }
	}

	/** 请求查询投资者结算结果 */
	public int ReqQrySettlementInfo(CThostFtdcQrySettlementInfoField f) throws JctpException {
		int reqId = getNextRequestId();
		ReqQrySettlementInfo(f,reqId);
		return reqId;
	}
	/** 请求查询投资者结算结果 */
	public void ReqQrySettlementInfo(CThostFtdcQrySettlementInfoField f, int nRequestID) throws JctpException {
		checkConnected();
		autoSleepReqQry();
		byte[] a1 = CThostFtdcQrySettlementInfoField.toBytes(f);
		int r = JctpApi.traderapiReq(this,nativeApiPtr, MethodId_TraderApi_ReqQrySettlementInfo, a1, nRequestID);
		if ( r!=0 ) throw new JctpException(r);
	}
	/** 请求查询投资者结算结果(SYNC) */
	public CThostFtdcSettlementInfoField SyncReqQrySettlementInfo(CThostFtdcQrySettlementInfoField f) throws JctpException {
		SyncRequest syncReq = createSyncRequest(false);
		try{
			ReqQrySettlementInfo(f,syncReq.requestId);
			return (CThostFtdcSettlementInfoField)waitForRsp(syncReq);
		}finally{ removeSyncRequest(syncReq); }
	}
	/** 请求查询投资者结算结果(SYNC ALL) */
	public CThostFtdcSettlementInfoField[] SyncAllReqQrySettlementInfo(CThostFtdcQrySettlementInfoField f) throws JctpException {
		SyncRequest syncReq = createSyncRequest(true);
		try{
			ReqQrySettlementInfo(f,syncReq.requestId);
			LinkedList list = waitForAllRsps(syncReq);
			return (CThostFtdcSettlementInfoField[])list.toArray(new CThostFtdcSettlementInfoField[list.size()]);
		}finally{ removeSyncRequest(syncReq); }
	}

	/** 请求查询转帐银行 */
	public int ReqQryTransferBank(CThostFtdcQryTransferBankField f) throws JctpException {
		int reqId = getNextRequestId();
		ReqQryTransferBank(f,reqId);
		return reqId;
	}
	/** 请求查询转帐银行 */
	public void ReqQryTransferBank(CThostFtdcQryTransferBankField f, int nRequestID) throws JctpException {
		checkConnected();
		autoSleepReqQry();
		byte[] a1 = CThostFtdcQryTransferBankField.toBytes(f);
		int r = JctpApi.traderapiReq(this,nativeApiPtr, MethodId_TraderApi_ReqQryTransferBank, a1, nRequestID);
		if ( r!=0 ) throw new JctpException(r);
	}
	/** 请求查询转帐银行(SYNC) */
	public CThostFtdcTransferBankField SyncReqQryTransferBank(CThostFtdcQryTransferBankField f) throws JctpException {
		SyncRequest syncReq = createSyncRequest(false);
		try{
			ReqQryTransferBank(f,syncReq.requestId);
			return (CThostFtdcTransferBankField)waitForRsp(syncReq);
		}finally{ removeSyncRequest(syncReq); }
	}
	/** 请求查询转帐银行(SYNC ALL) */
	public CThostFtdcTransferBankField[] SyncAllReqQryTransferBank(CThostFtdcQryTransferBankField f) throws JctpException {
		SyncRequest syncReq = createSyncRequest(true);
		try{
			ReqQryTransferBank(f,syncReq.requestId);
			LinkedList list = waitForAllRsps(syncReq);
			return (CThostFtdcTransferBankField[])list.toArray(new CThostFtdcTransferBankField[list.size()]);
		}finally{ removeSyncRequest(syncReq); }
	}

	/** 请求查询投资者持仓明细 */
	public int ReqQryInvestorPositionDetail(CThostFtdcQryInvestorPositionDetailField f) throws JctpException {
		int reqId = getNextRequestId();
		ReqQryInvestorPositionDetail(f,reqId);
		return reqId;
	}
	/** 请求查询投资者持仓明细 */
	public void ReqQryInvestorPositionDetail(CThostFtdcQryInvestorPositionDetailField f, int nRequestID) throws JctpException {
		checkConnected();
		autoSleepReqQry();
		byte[] a1 = CThostFtdcQryInvestorPositionDetailField.toBytes(f);
		int r = JctpApi.traderapiReq(this,nativeApiPtr, MethodId_TraderApi_ReqQryInvestorPositionDetail, a1, nRequestID);
		if ( r!=0 ) throw new JctpException(r);
	}
	/** 请求查询投资者持仓明细(SYNC) */
	public CThostFtdcInvestorPositionDetailField SyncReqQryInvestorPositionDetail(CThostFtdcQryInvestorPositionDetailField f) throws JctpException {
		SyncRequest syncReq = createSyncRequest(false);
		try{
			ReqQryInvestorPositionDetail(f,syncReq.requestId);
			return (CThostFtdcInvestorPositionDetailField)waitForRsp(syncReq);
		}finally{ removeSyncRequest(syncReq); }
	}
	/** 请求查询投资者持仓明细(SYNC ALL) */
	public CThostFtdcInvestorPositionDetailField[] SyncAllReqQryInvestorPositionDetail(CThostFtdcQryInvestorPositionDetailField f) throws JctpException {
		SyncRequest syncReq = createSyncRequest(true);
		try{
			ReqQryInvestorPositionDetail(f,syncReq.requestId);
			LinkedList list = waitForAllRsps(syncReq);
			return (CThostFtdcInvestorPositionDetailField[])list.toArray(new CThostFtdcInvestorPositionDetailField[list.size()]);
		}finally{ removeSyncRequest(syncReq); }
	}

	/** 请求查询客户通知 */
	public int ReqQryNotice(CThostFtdcQryNoticeField f) throws JctpException {
		int reqId = getNextRequestId();
		ReqQryNotice(f,reqId);
		return reqId;
	}
	/** 请求查询客户通知 */
	public void ReqQryNotice(CThostFtdcQryNoticeField f, int nRequestID) throws JctpException {
		checkConnected();
		autoSleepReqQry();
		byte[] a1 = CThostFtdcQryNoticeField.toBytes(f);
		int r = JctpApi.traderapiReq(this,nativeApiPtr, MethodId_TraderApi_ReqQryNotice, a1, nRequestID);
		if ( r!=0 ) throw new JctpException(r);
	}
	/** 请求查询客户通知(SYNC) */
	public CThostFtdcNoticeField SyncReqQryNotice(CThostFtdcQryNoticeField f) throws JctpException {
		SyncRequest syncReq = createSyncRequest(false);
		try{
			ReqQryNotice(f,syncReq.requestId);
			return (CThostFtdcNoticeField)waitForRsp(syncReq);
		}finally{ removeSyncRequest(syncReq); }
	}
	/** 请求查询客户通知(SYNC ALL) */
	public CThostFtdcNoticeField[] SyncAllReqQryNotice(CThostFtdcQryNoticeField f) throws JctpException {
		SyncRequest syncReq = createSyncRequest(true);
		try{
			ReqQryNotice(f,syncReq.requestId);
			LinkedList list = waitForAllRsps(syncReq);
			return (CThostFtdcNoticeField[])list.toArray(new CThostFtdcNoticeField[list.size()]);
		}finally{ removeSyncRequest(syncReq); }
	}

	/** 请求查询结算信息确认 */
	public int ReqQrySettlementInfoConfirm(CThostFtdcQrySettlementInfoConfirmField f) throws JctpException {
		int reqId = getNextRequestId();
		ReqQrySettlementInfoConfirm(f,reqId);
		return reqId;
	}
	/** 请求查询结算信息确认 */
	public void ReqQrySettlementInfoConfirm(CThostFtdcQrySettlementInfoConfirmField f, int nRequestID) throws JctpException {
		checkConnected();
		autoSleepReqQry();
		byte[] a1 = CThostFtdcQrySettlementInfoConfirmField.toBytes(f);
		int r = JctpApi.traderapiReq(this,nativeApiPtr, MethodId_TraderApi_ReqQrySettlementInfoConfirm, a1, nRequestID);
		if ( r!=0 ) throw new JctpException(r);
	}
	/** 请求查询结算信息确认(SYNC) */
	public CThostFtdcSettlementInfoConfirmField SyncReqQrySettlementInfoConfirm(CThostFtdcQrySettlementInfoConfirmField f) throws JctpException {
		SyncRequest syncReq = createSyncRequest(false);
		try{
			ReqQrySettlementInfoConfirm(f,syncReq.requestId);
			return (CThostFtdcSettlementInfoConfirmField)waitForRsp(syncReq);
		}finally{ removeSyncRequest(syncReq); }
	}
	/** 请求查询结算信息确认(SYNC ALL) */
	public CThostFtdcSettlementInfoConfirmField[] SyncAllReqQrySettlementInfoConfirm(CThostFtdcQrySettlementInfoConfirmField f) throws JctpException {
		SyncRequest syncReq = createSyncRequest(true);
		try{
			ReqQrySettlementInfoConfirm(f,syncReq.requestId);
			LinkedList list = waitForAllRsps(syncReq);
			return (CThostFtdcSettlementInfoConfirmField[])list.toArray(new CThostFtdcSettlementInfoConfirmField[list.size()]);
		}finally{ removeSyncRequest(syncReq); }
	}

	/** 请求查询投资者持仓明细 */
	public int ReqQryInvestorPositionCombineDetail(CThostFtdcQryInvestorPositionCombineDetailField f) throws JctpException {
		int reqId = getNextRequestId();
		ReqQryInvestorPositionCombineDetail(f,reqId);
		return reqId;
	}
	/** 请求查询投资者持仓明细 */
	public void ReqQryInvestorPositionCombineDetail(CThostFtdcQryInvestorPositionCombineDetailField f, int nRequestID) throws JctpException {
		checkConnected();
		autoSleepReqQry();
		byte[] a1 = CThostFtdcQryInvestorPositionCombineDetailField.toBytes(f);
		int r = JctpApi.traderapiReq(this,nativeApiPtr, MethodId_TraderApi_ReqQryInvestorPositionCombineDetail, a1, nRequestID);
		if ( r!=0 ) throw new JctpException(r);
	}
	/** 请求查询投资者持仓明细(SYNC) */
	public CThostFtdcInvestorPositionCombineDetailField SyncReqQryInvestorPositionCombineDetail(CThostFtdcQryInvestorPositionCombineDetailField f) throws JctpException {
		SyncRequest syncReq = createSyncRequest(false);
		try{
			ReqQryInvestorPositionCombineDetail(f,syncReq.requestId);
			return (CThostFtdcInvestorPositionCombineDetailField)waitForRsp(syncReq);
		}finally{ removeSyncRequest(syncReq); }
	}
	/** 请求查询投资者持仓明细(SYNC ALL) */
	public CThostFtdcInvestorPositionCombineDetailField[] SyncAllReqQryInvestorPositionCombineDetail(CThostFtdcQryInvestorPositionCombineDetailField f) throws JctpException {
		SyncRequest syncReq = createSyncRequest(true);
		try{
			ReqQryInvestorPositionCombineDetail(f,syncReq.requestId);
			LinkedList list = waitForAllRsps(syncReq);
			return (CThostFtdcInvestorPositionCombineDetailField[])list.toArray(new CThostFtdcInvestorPositionCombineDetailField[list.size()]);
		}finally{ removeSyncRequest(syncReq); }
	}

	/** 请求查询保证金监管系统经纪公司资金账户密钥 */
	public int ReqQryCFMMCTradingAccountKey(CThostFtdcQryCFMMCTradingAccountKeyField f) throws JctpException {
		int reqId = getNextRequestId();
		ReqQryCFMMCTradingAccountKey(f,reqId);
		return reqId;
	}
	/** 请求查询保证金监管系统经纪公司资金账户密钥 */
	public void ReqQryCFMMCTradingAccountKey(CThostFtdcQryCFMMCTradingAccountKeyField f, int nRequestID) throws JctpException {
		checkConnected();
		autoSleepReqQry();
		byte[] a1 = CThostFtdcQryCFMMCTradingAccountKeyField.toBytes(f);
		int r = JctpApi.traderapiReq(this,nativeApiPtr, MethodId_TraderApi_ReqQryCFMMCTradingAccountKey, a1, nRequestID);
		if ( r!=0 ) throw new JctpException(r);
	}
	/** 请求查询保证金监管系统经纪公司资金账户密钥(SYNC) */
	public CThostFtdcCFMMCTradingAccountKeyField SyncReqQryCFMMCTradingAccountKey(CThostFtdcQryCFMMCTradingAccountKeyField f) throws JctpException {
		SyncRequest syncReq = createSyncRequest(false);
		try{
			ReqQryCFMMCTradingAccountKey(f,syncReq.requestId);
			return (CThostFtdcCFMMCTradingAccountKeyField)waitForRsp(syncReq);
		}finally{ removeSyncRequest(syncReq); }
	}
	/** 请求查询保证金监管系统经纪公司资金账户密钥(SYNC ALL) */
	public CThostFtdcCFMMCTradingAccountKeyField[] SyncAllReqQryCFMMCTradingAccountKey(CThostFtdcQryCFMMCTradingAccountKeyField f) throws JctpException {
		SyncRequest syncReq = createSyncRequest(true);
		try{
			ReqQryCFMMCTradingAccountKey(f,syncReq.requestId);
			LinkedList list = waitForAllRsps(syncReq);
			return (CThostFtdcCFMMCTradingAccountKeyField[])list.toArray(new CThostFtdcCFMMCTradingAccountKeyField[list.size()]);
		}finally{ removeSyncRequest(syncReq); }
	}

	/** 请求查询仓单折抵信息 */
	public int ReqQryEWarrantOffset(CThostFtdcQryEWarrantOffsetField f) throws JctpException {
		int reqId = getNextRequestId();
		ReqQryEWarrantOffset(f,reqId);
		return reqId;
	}
	/** 请求查询仓单折抵信息 */
	public void ReqQryEWarrantOffset(CThostFtdcQryEWarrantOffsetField f, int nRequestID) throws JctpException {
		checkConnected();
		autoSleepReqQry();
		byte[] a1 = CThostFtdcQryEWarrantOffsetField.toBytes(f);
		int r = JctpApi.traderapiReq(this,nativeApiPtr, MethodId_TraderApi_ReqQryEWarrantOffset, a1, nRequestID);
		if ( r!=0 ) throw new JctpException(r);
	}
	/** 请求查询仓单折抵信息(SYNC) */
	public CThostFtdcEWarrantOffsetField SyncReqQryEWarrantOffset(CThostFtdcQryEWarrantOffsetField f) throws JctpException {
		SyncRequest syncReq = createSyncRequest(false);
		try{
			ReqQryEWarrantOffset(f,syncReq.requestId);
			return (CThostFtdcEWarrantOffsetField)waitForRsp(syncReq);
		}finally{ removeSyncRequest(syncReq); }
	}
	/** 请求查询仓单折抵信息(SYNC ALL) */
	public CThostFtdcEWarrantOffsetField[] SyncAllReqQryEWarrantOffset(CThostFtdcQryEWarrantOffsetField f) throws JctpException {
		SyncRequest syncReq = createSyncRequest(true);
		try{
			ReqQryEWarrantOffset(f,syncReq.requestId);
			LinkedList list = waitForAllRsps(syncReq);
			return (CThostFtdcEWarrantOffsetField[])list.toArray(new CThostFtdcEWarrantOffsetField[list.size()]);
		}finally{ removeSyncRequest(syncReq); }
	}

	/** 请求查询投资者品种/跨品种保证金 */
	public int ReqQryInvestorProductGroupMargin(CThostFtdcQryInvestorProductGroupMarginField f) throws JctpException {
		int reqId = getNextRequestId();
		ReqQryInvestorProductGroupMargin(f,reqId);
		return reqId;
	}
	/** 请求查询投资者品种/跨品种保证金 */
	public void ReqQryInvestorProductGroupMargin(CThostFtdcQryInvestorProductGroupMarginField f, int nRequestID) throws JctpException {
		checkConnected();
		autoSleepReqQry();
		byte[] a1 = CThostFtdcQryInvestorProductGroupMarginField.toBytes(f);
		int r = JctpApi.traderapiReq(this,nativeApiPtr, MethodId_TraderApi_ReqQryInvestorProductGroupMargin, a1, nRequestID);
		if ( r!=0 ) throw new JctpException(r);
	}
	/** 请求查询投资者品种/跨品种保证金(SYNC) */
	public CThostFtdcInvestorProductGroupMarginField SyncReqQryInvestorProductGroupMargin(CThostFtdcQryInvestorProductGroupMarginField f) throws JctpException {
		SyncRequest syncReq = createSyncRequest(false);
		try{
			ReqQryInvestorProductGroupMargin(f,syncReq.requestId);
			return (CThostFtdcInvestorProductGroupMarginField)waitForRsp(syncReq);
		}finally{ removeSyncRequest(syncReq); }
	}
	/** 请求查询投资者品种/跨品种保证金(SYNC ALL) */
	public CThostFtdcInvestorProductGroupMarginField[] SyncAllReqQryInvestorProductGroupMargin(CThostFtdcQryInvestorProductGroupMarginField f) throws JctpException {
		SyncRequest syncReq = createSyncRequest(true);
		try{
			ReqQryInvestorProductGroupMargin(f,syncReq.requestId);
			LinkedList list = waitForAllRsps(syncReq);
			return (CThostFtdcInvestorProductGroupMarginField[])list.toArray(new CThostFtdcInvestorProductGroupMarginField[list.size()]);
		}finally{ removeSyncRequest(syncReq); }
	}

	/** 请求查询交易所保证金率 */
	public int ReqQryExchangeMarginRate(CThostFtdcQryExchangeMarginRateField f) throws JctpException {
		int reqId = getNextRequestId();
		ReqQryExchangeMarginRate(f,reqId);
		return reqId;
	}
	/** 请求查询交易所保证金率 */
	public void ReqQryExchangeMarginRate(CThostFtdcQryExchangeMarginRateField f, int nRequestID) throws JctpException {
		checkConnected();
		autoSleepReqQry();
		byte[] a1 = CThostFtdcQryExchangeMarginRateField.toBytes(f);
		int r = JctpApi.traderapiReq(this,nativeApiPtr, MethodId_TraderApi_ReqQryExchangeMarginRate, a1, nRequestID);
		if ( r!=0 ) throw new JctpException(r);
	}
	/** 请求查询交易所保证金率(SYNC) */
	public CThostFtdcExchangeMarginRateField SyncReqQryExchangeMarginRate(CThostFtdcQryExchangeMarginRateField f) throws JctpException {
		SyncRequest syncReq = createSyncRequest(false);
		try{
			ReqQryExchangeMarginRate(f,syncReq.requestId);
			return (CThostFtdcExchangeMarginRateField)waitForRsp(syncReq);
		}finally{ removeSyncRequest(syncReq); }
	}
	/** 请求查询交易所保证金率(SYNC ALL) */
	public CThostFtdcExchangeMarginRateField[] SyncAllReqQryExchangeMarginRate(CThostFtdcQryExchangeMarginRateField f) throws JctpException {
		SyncRequest syncReq = createSyncRequest(true);
		try{
			ReqQryExchangeMarginRate(f,syncReq.requestId);
			LinkedList list = waitForAllRsps(syncReq);
			return (CThostFtdcExchangeMarginRateField[])list.toArray(new CThostFtdcExchangeMarginRateField[list.size()]);
		}finally{ removeSyncRequest(syncReq); }
	}

	/** 请求查询交易所调整保证金率 */
	public int ReqQryExchangeMarginRateAdjust(CThostFtdcQryExchangeMarginRateAdjustField f) throws JctpException {
		int reqId = getNextRequestId();
		ReqQryExchangeMarginRateAdjust(f,reqId);
		return reqId;
	}
	/** 请求查询交易所调整保证金率 */
	public void ReqQryExchangeMarginRateAdjust(CThostFtdcQryExchangeMarginRateAdjustField f, int nRequestID) throws JctpException {
		checkConnected();
		autoSleepReqQry();
		byte[] a1 = CThostFtdcQryExchangeMarginRateAdjustField.toBytes(f);
		int r = JctpApi.traderapiReq(this,nativeApiPtr, MethodId_TraderApi_ReqQryExchangeMarginRateAdjust, a1, nRequestID);
		if ( r!=0 ) throw new JctpException(r);
	}
	/** 请求查询交易所调整保证金率(SYNC) */
	public CThostFtdcExchangeMarginRateAdjustField SyncReqQryExchangeMarginRateAdjust(CThostFtdcQryExchangeMarginRateAdjustField f) throws JctpException {
		SyncRequest syncReq = createSyncRequest(false);
		try{
			ReqQryExchangeMarginRateAdjust(f,syncReq.requestId);
			return (CThostFtdcExchangeMarginRateAdjustField)waitForRsp(syncReq);
		}finally{ removeSyncRequest(syncReq); }
	}
	/** 请求查询交易所调整保证金率(SYNC ALL) */
	public CThostFtdcExchangeMarginRateAdjustField[] SyncAllReqQryExchangeMarginRateAdjust(CThostFtdcQryExchangeMarginRateAdjustField f) throws JctpException {
		SyncRequest syncReq = createSyncRequest(true);
		try{
			ReqQryExchangeMarginRateAdjust(f,syncReq.requestId);
			LinkedList list = waitForAllRsps(syncReq);
			return (CThostFtdcExchangeMarginRateAdjustField[])list.toArray(new CThostFtdcExchangeMarginRateAdjustField[list.size()]);
		}finally{ removeSyncRequest(syncReq); }
	}

	/** 请求查询汇率 */
	public int ReqQryExchangeRate(CThostFtdcQryExchangeRateField f) throws JctpException {
		int reqId = getNextRequestId();
		ReqQryExchangeRate(f,reqId);
		return reqId;
	}
	/** 请求查询汇率 */
	public void ReqQryExchangeRate(CThostFtdcQryExchangeRateField f, int nRequestID) throws JctpException {
		checkConnected();
		autoSleepReqQry();
		byte[] a1 = CThostFtdcQryExchangeRateField.toBytes(f);
		int r = JctpApi.traderapiReq(this,nativeApiPtr, MethodId_TraderApi_ReqQryExchangeRate, a1, nRequestID);
		if ( r!=0 ) throw new JctpException(r);
	}
	/** 请求查询汇率(SYNC) */
	public CThostFtdcExchangeRateField SyncReqQryExchangeRate(CThostFtdcQryExchangeRateField f) throws JctpException {
		SyncRequest syncReq = createSyncRequest(false);
		try{
			ReqQryExchangeRate(f,syncReq.requestId);
			return (CThostFtdcExchangeRateField)waitForRsp(syncReq);
		}finally{ removeSyncRequest(syncReq); }
	}
	/** 请求查询汇率(SYNC ALL) */
	public CThostFtdcExchangeRateField[] SyncAllReqQryExchangeRate(CThostFtdcQryExchangeRateField f) throws JctpException {
		SyncRequest syncReq = createSyncRequest(true);
		try{
			ReqQryExchangeRate(f,syncReq.requestId);
			LinkedList list = waitForAllRsps(syncReq);
			return (CThostFtdcExchangeRateField[])list.toArray(new CThostFtdcExchangeRateField[list.size()]);
		}finally{ removeSyncRequest(syncReq); }
	}

	/** 请求查询二级代理操作员银期权限 */
	public int ReqQrySecAgentACIDMap(CThostFtdcQrySecAgentACIDMapField f) throws JctpException {
		int reqId = getNextRequestId();
		ReqQrySecAgentACIDMap(f,reqId);
		return reqId;
	}
	/** 请求查询二级代理操作员银期权限 */
	public void ReqQrySecAgentACIDMap(CThostFtdcQrySecAgentACIDMapField f, int nRequestID) throws JctpException {
		checkConnected();
		autoSleepReqQry();
		byte[] a1 = CThostFtdcQrySecAgentACIDMapField.toBytes(f);
		int r = JctpApi.traderapiReq(this,nativeApiPtr, MethodId_TraderApi_ReqQrySecAgentACIDMap, a1, nRequestID);
		if ( r!=0 ) throw new JctpException(r);
	}
	/** 请求查询二级代理操作员银期权限(SYNC) */
	public CThostFtdcSecAgentACIDMapField SyncReqQrySecAgentACIDMap(CThostFtdcQrySecAgentACIDMapField f) throws JctpException {
		SyncRequest syncReq = createSyncRequest(false);
		try{
			ReqQrySecAgentACIDMap(f,syncReq.requestId);
			return (CThostFtdcSecAgentACIDMapField)waitForRsp(syncReq);
		}finally{ removeSyncRequest(syncReq); }
	}
	/** 请求查询二级代理操作员银期权限(SYNC ALL) */
	public CThostFtdcSecAgentACIDMapField[] SyncAllReqQrySecAgentACIDMap(CThostFtdcQrySecAgentACIDMapField f) throws JctpException {
		SyncRequest syncReq = createSyncRequest(true);
		try{
			ReqQrySecAgentACIDMap(f,syncReq.requestId);
			LinkedList list = waitForAllRsps(syncReq);
			return (CThostFtdcSecAgentACIDMapField[])list.toArray(new CThostFtdcSecAgentACIDMapField[list.size()]);
		}finally{ removeSyncRequest(syncReq); }
	}

	/** 请求查询期权交易成本 */
	public int ReqQryOptionInstrTradeCost(CThostFtdcQryOptionInstrTradeCostField f) throws JctpException {
		int reqId = getNextRequestId();
		ReqQryOptionInstrTradeCost(f,reqId);
		return reqId;
	}
	/** 请求查询期权交易成本 */
	public void ReqQryOptionInstrTradeCost(CThostFtdcQryOptionInstrTradeCostField f, int nRequestID) throws JctpException {
		checkConnected();
		autoSleepReqQry();
		byte[] a1 = CThostFtdcQryOptionInstrTradeCostField.toBytes(f);
		int r = JctpApi.traderapiReq(this,nativeApiPtr, MethodId_TraderApi_ReqQryOptionInstrTradeCost, a1, nRequestID);
		if ( r!=0 ) throw new JctpException(r);
	}
	/** 请求查询期权交易成本(SYNC) */
	public CThostFtdcOptionInstrTradeCostField SyncReqQryOptionInstrTradeCost(CThostFtdcQryOptionInstrTradeCostField f) throws JctpException {
		SyncRequest syncReq = createSyncRequest(false);
		try{
			ReqQryOptionInstrTradeCost(f,syncReq.requestId);
			return (CThostFtdcOptionInstrTradeCostField)waitForRsp(syncReq);
		}finally{ removeSyncRequest(syncReq); }
	}
	/** 请求查询期权交易成本(SYNC ALL) */
	public CThostFtdcOptionInstrTradeCostField[] SyncAllReqQryOptionInstrTradeCost(CThostFtdcQryOptionInstrTradeCostField f) throws JctpException {
		SyncRequest syncReq = createSyncRequest(true);
		try{
			ReqQryOptionInstrTradeCost(f,syncReq.requestId);
			LinkedList list = waitForAllRsps(syncReq);
			return (CThostFtdcOptionInstrTradeCostField[])list.toArray(new CThostFtdcOptionInstrTradeCostField[list.size()]);
		}finally{ removeSyncRequest(syncReq); }
	}

	/** 请求查询期权合约手续费 */
	public int ReqQryOptionInstrCommRate(CThostFtdcQryOptionInstrCommRateField f) throws JctpException {
		int reqId = getNextRequestId();
		ReqQryOptionInstrCommRate(f,reqId);
		return reqId;
	}
	/** 请求查询期权合约手续费 */
	public void ReqQryOptionInstrCommRate(CThostFtdcQryOptionInstrCommRateField f, int nRequestID) throws JctpException {
		checkConnected();
		autoSleepReqQry();
		byte[] a1 = CThostFtdcQryOptionInstrCommRateField.toBytes(f);
		int r = JctpApi.traderapiReq(this,nativeApiPtr, MethodId_TraderApi_ReqQryOptionInstrCommRate, a1, nRequestID);
		if ( r!=0 ) throw new JctpException(r);
	}
	/** 请求查询期权合约手续费(SYNC) */
	public CThostFtdcOptionInstrCommRateField SyncReqQryOptionInstrCommRate(CThostFtdcQryOptionInstrCommRateField f) throws JctpException {
		SyncRequest syncReq = createSyncRequest(false);
		try{
			ReqQryOptionInstrCommRate(f,syncReq.requestId);
			return (CThostFtdcOptionInstrCommRateField)waitForRsp(syncReq);
		}finally{ removeSyncRequest(syncReq); }
	}
	/** 请求查询期权合约手续费(SYNC ALL) */
	public CThostFtdcOptionInstrCommRateField[] SyncAllReqQryOptionInstrCommRate(CThostFtdcQryOptionInstrCommRateField f) throws JctpException {
		SyncRequest syncReq = createSyncRequest(true);
		try{
			ReqQryOptionInstrCommRate(f,syncReq.requestId);
			LinkedList list = waitForAllRsps(syncReq);
			return (CThostFtdcOptionInstrCommRateField[])list.toArray(new CThostFtdcOptionInstrCommRateField[list.size()]);
		}finally{ removeSyncRequest(syncReq); }
	}

	/** 请求查询执行宣告 */
	public int ReqQryExecOrder(CThostFtdcQryExecOrderField f) throws JctpException {
		int reqId = getNextRequestId();
		ReqQryExecOrder(f,reqId);
		return reqId;
	}
	/** 请求查询执行宣告 */
	public void ReqQryExecOrder(CThostFtdcQryExecOrderField f, int nRequestID) throws JctpException {
		checkConnected();
		autoSleepReqQry();
		byte[] a1 = CThostFtdcQryExecOrderField.toBytes(f);
		int r = JctpApi.traderapiReq(this,nativeApiPtr, MethodId_TraderApi_ReqQryExecOrder, a1, nRequestID);
		if ( r!=0 ) throw new JctpException(r);
	}
	/** 请求查询执行宣告(SYNC) */
	public CThostFtdcExecOrderField SyncReqQryExecOrder(CThostFtdcQryExecOrderField f) throws JctpException {
		SyncRequest syncReq = createSyncRequest(false);
		try{
			ReqQryExecOrder(f,syncReq.requestId);
			return (CThostFtdcExecOrderField)waitForRsp(syncReq);
		}finally{ removeSyncRequest(syncReq); }
	}
	/** 请求查询执行宣告(SYNC ALL) */
	public CThostFtdcExecOrderField[] SyncAllReqQryExecOrder(CThostFtdcQryExecOrderField f) throws JctpException {
		SyncRequest syncReq = createSyncRequest(true);
		try{
			ReqQryExecOrder(f,syncReq.requestId);
			LinkedList list = waitForAllRsps(syncReq);
			return (CThostFtdcExecOrderField[])list.toArray(new CThostFtdcExecOrderField[list.size()]);
		}finally{ removeSyncRequest(syncReq); }
	}

	/** 请求查询询价 */
	public int ReqQryForQuote(CThostFtdcQryForQuoteField f) throws JctpException {
		int reqId = getNextRequestId();
		ReqQryForQuote(f,reqId);
		return reqId;
	}
	/** 请求查询询价 */
	public void ReqQryForQuote(CThostFtdcQryForQuoteField f, int nRequestID) throws JctpException {
		checkConnected();
		autoSleepReqQry();
		byte[] a1 = CThostFtdcQryForQuoteField.toBytes(f);
		int r = JctpApi.traderapiReq(this,nativeApiPtr, MethodId_TraderApi_ReqQryForQuote, a1, nRequestID);
		if ( r!=0 ) throw new JctpException(r);
	}
	/** 请求查询询价(SYNC) */
	public CThostFtdcForQuoteField SyncReqQryForQuote(CThostFtdcQryForQuoteField f) throws JctpException {
		SyncRequest syncReq = createSyncRequest(false);
		try{
			ReqQryForQuote(f,syncReq.requestId);
			return (CThostFtdcForQuoteField)waitForRsp(syncReq);
		}finally{ removeSyncRequest(syncReq); }
	}
	/** 请求查询询价(SYNC ALL) */
	public CThostFtdcForQuoteField[] SyncAllReqQryForQuote(CThostFtdcQryForQuoteField f) throws JctpException {
		SyncRequest syncReq = createSyncRequest(true);
		try{
			ReqQryForQuote(f,syncReq.requestId);
			LinkedList list = waitForAllRsps(syncReq);
			return (CThostFtdcForQuoteField[])list.toArray(new CThostFtdcForQuoteField[list.size()]);
		}finally{ removeSyncRequest(syncReq); }
	}

	/** 请求查询报价 */
	public int ReqQryQuote(CThostFtdcQryQuoteField f) throws JctpException {
		int reqId = getNextRequestId();
		ReqQryQuote(f,reqId);
		return reqId;
	}
	/** 请求查询报价 */
	public void ReqQryQuote(CThostFtdcQryQuoteField f, int nRequestID) throws JctpException {
		checkConnected();
		autoSleepReqQry();
		byte[] a1 = CThostFtdcQryQuoteField.toBytes(f);
		int r = JctpApi.traderapiReq(this,nativeApiPtr, MethodId_TraderApi_ReqQryQuote, a1, nRequestID);
		if ( r!=0 ) throw new JctpException(r);
	}
	/** 请求查询报价(SYNC) */
	public CThostFtdcQuoteField SyncReqQryQuote(CThostFtdcQryQuoteField f) throws JctpException {
		SyncRequest syncReq = createSyncRequest(false);
		try{
			ReqQryQuote(f,syncReq.requestId);
			return (CThostFtdcQuoteField)waitForRsp(syncReq);
		}finally{ removeSyncRequest(syncReq); }
	}
	/** 请求查询报价(SYNC ALL) */
	public CThostFtdcQuoteField[] SyncAllReqQryQuote(CThostFtdcQryQuoteField f) throws JctpException {
		SyncRequest syncReq = createSyncRequest(true);
		try{
			ReqQryQuote(f,syncReq.requestId);
			LinkedList list = waitForAllRsps(syncReq);
			return (CThostFtdcQuoteField[])list.toArray(new CThostFtdcQuoteField[list.size()]);
		}finally{ removeSyncRequest(syncReq); }
	}

	/** 请求查询转帐流水 */
	public int ReqQryTransferSerial(CThostFtdcQryTransferSerialField f) throws JctpException {
		int reqId = getNextRequestId();
		ReqQryTransferSerial(f,reqId);
		return reqId;
	}
	/** 请求查询转帐流水 */
	public void ReqQryTransferSerial(CThostFtdcQryTransferSerialField f, int nRequestID) throws JctpException {
		checkConnected();
		autoSleepReqQry();
		byte[] a1 = CThostFtdcQryTransferSerialField.toBytes(f);
		int r = JctpApi.traderapiReq(this,nativeApiPtr, MethodId_TraderApi_ReqQryTransferSerial, a1, nRequestID);
		if ( r!=0 ) throw new JctpException(r);
	}
	/** 请求查询转帐流水(SYNC) */
	public CThostFtdcTransferSerialField SyncReqQryTransferSerial(CThostFtdcQryTransferSerialField f) throws JctpException {
		SyncRequest syncReq = createSyncRequest(false);
		try{
			ReqQryTransferSerial(f,syncReq.requestId);
			return (CThostFtdcTransferSerialField)waitForRsp(syncReq);
		}finally{ removeSyncRequest(syncReq); }
	}
	/** 请求查询转帐流水(SYNC ALL) */
	public CThostFtdcTransferSerialField[] SyncAllReqQryTransferSerial(CThostFtdcQryTransferSerialField f) throws JctpException {
		SyncRequest syncReq = createSyncRequest(true);
		try{
			ReqQryTransferSerial(f,syncReq.requestId);
			LinkedList list = waitForAllRsps(syncReq);
			return (CThostFtdcTransferSerialField[])list.toArray(new CThostFtdcTransferSerialField[list.size()]);
		}finally{ removeSyncRequest(syncReq); }
	}

	/** 请求查询银期签约关系 */
	public int ReqQryAccountregister(CThostFtdcQryAccountregisterField f) throws JctpException {
		int reqId = getNextRequestId();
		ReqQryAccountregister(f,reqId);
		return reqId;
	}
	/** 请求查询银期签约关系 */
	public void ReqQryAccountregister(CThostFtdcQryAccountregisterField f, int nRequestID) throws JctpException {
		checkConnected();
		autoSleepReqQry();
		byte[] a1 = CThostFtdcQryAccountregisterField.toBytes(f);
		int r = JctpApi.traderapiReq(this,nativeApiPtr, MethodId_TraderApi_ReqQryAccountregister, a1, nRequestID);
		if ( r!=0 ) throw new JctpException(r);
	}
	/** 请求查询银期签约关系(SYNC) */
	public CThostFtdcAccountregisterField SyncReqQryAccountregister(CThostFtdcQryAccountregisterField f) throws JctpException {
		SyncRequest syncReq = createSyncRequest(false);
		try{
			ReqQryAccountregister(f,syncReq.requestId);
			return (CThostFtdcAccountregisterField)waitForRsp(syncReq);
		}finally{ removeSyncRequest(syncReq); }
	}
	/** 请求查询银期签约关系(SYNC ALL) */
	public CThostFtdcAccountregisterField[] SyncAllReqQryAccountregister(CThostFtdcQryAccountregisterField f) throws JctpException {
		SyncRequest syncReq = createSyncRequest(true);
		try{
			ReqQryAccountregister(f,syncReq.requestId);
			LinkedList list = waitForAllRsps(syncReq);
			return (CThostFtdcAccountregisterField[])list.toArray(new CThostFtdcAccountregisterField[list.size()]);
		}finally{ removeSyncRequest(syncReq); }
	}

	/** 请求查询签约银行 */
	public int ReqQryContractBank(CThostFtdcQryContractBankField f) throws JctpException {
		int reqId = getNextRequestId();
		ReqQryContractBank(f,reqId);
		return reqId;
	}
	/** 请求查询签约银行 */
	public void ReqQryContractBank(CThostFtdcQryContractBankField f, int nRequestID) throws JctpException {
		checkConnected();
		autoSleepReqQry();
		byte[] a1 = CThostFtdcQryContractBankField.toBytes(f);
		int r = JctpApi.traderapiReq(this,nativeApiPtr, MethodId_TraderApi_ReqQryContractBank, a1, nRequestID);
		if ( r!=0 ) throw new JctpException(r);
	}
	/** 请求查询签约银行(SYNC) */
	public CThostFtdcContractBankField SyncReqQryContractBank(CThostFtdcQryContractBankField f) throws JctpException {
		SyncRequest syncReq = createSyncRequest(false);
		try{
			ReqQryContractBank(f,syncReq.requestId);
			return (CThostFtdcContractBankField)waitForRsp(syncReq);
		}finally{ removeSyncRequest(syncReq); }
	}
	/** 请求查询签约银行(SYNC ALL) */
	public CThostFtdcContractBankField[] SyncAllReqQryContractBank(CThostFtdcQryContractBankField f) throws JctpException {
		SyncRequest syncReq = createSyncRequest(true);
		try{
			ReqQryContractBank(f,syncReq.requestId);
			LinkedList list = waitForAllRsps(syncReq);
			return (CThostFtdcContractBankField[])list.toArray(new CThostFtdcContractBankField[list.size()]);
		}finally{ removeSyncRequest(syncReq); }
	}

	/** 请求查询预埋单 */
	public int ReqQryParkedOrder(CThostFtdcQryParkedOrderField f) throws JctpException {
		int reqId = getNextRequestId();
		ReqQryParkedOrder(f,reqId);
		return reqId;
	}
	/** 请求查询预埋单 */
	public void ReqQryParkedOrder(CThostFtdcQryParkedOrderField f, int nRequestID) throws JctpException {
		checkConnected();
		autoSleepReqQry();
		byte[] a1 = CThostFtdcQryParkedOrderField.toBytes(f);
		int r = JctpApi.traderapiReq(this,nativeApiPtr, MethodId_TraderApi_ReqQryParkedOrder, a1, nRequestID);
		if ( r!=0 ) throw new JctpException(r);
	}
	/** 请求查询预埋单(SYNC) */
	public CThostFtdcParkedOrderField SyncReqQryParkedOrder(CThostFtdcQryParkedOrderField f) throws JctpException {
		SyncRequest syncReq = createSyncRequest(false);
		try{
			ReqQryParkedOrder(f,syncReq.requestId);
			return (CThostFtdcParkedOrderField)waitForRsp(syncReq);
		}finally{ removeSyncRequest(syncReq); }
	}
	/** 请求查询预埋单(SYNC ALL) */
	public CThostFtdcParkedOrderField[] SyncAllReqQryParkedOrder(CThostFtdcQryParkedOrderField f) throws JctpException {
		SyncRequest syncReq = createSyncRequest(true);
		try{
			ReqQryParkedOrder(f,syncReq.requestId);
			LinkedList list = waitForAllRsps(syncReq);
			return (CThostFtdcParkedOrderField[])list.toArray(new CThostFtdcParkedOrderField[list.size()]);
		}finally{ removeSyncRequest(syncReq); }
	}

	/** 请求查询预埋撤单 */
	public int ReqQryParkedOrderAction(CThostFtdcQryParkedOrderActionField f) throws JctpException {
		int reqId = getNextRequestId();
		ReqQryParkedOrderAction(f,reqId);
		return reqId;
	}
	/** 请求查询预埋撤单 */
	public void ReqQryParkedOrderAction(CThostFtdcQryParkedOrderActionField f, int nRequestID) throws JctpException {
		checkConnected();
		autoSleepReqQry();
		byte[] a1 = CThostFtdcQryParkedOrderActionField.toBytes(f);
		int r = JctpApi.traderapiReq(this,nativeApiPtr, MethodId_TraderApi_ReqQryParkedOrderAction, a1, nRequestID);
		if ( r!=0 ) throw new JctpException(r);
	}
	/** 请求查询预埋撤单(SYNC) */
	public CThostFtdcParkedOrderActionField SyncReqQryParkedOrderAction(CThostFtdcQryParkedOrderActionField f) throws JctpException {
		SyncRequest syncReq = createSyncRequest(false);
		try{
			ReqQryParkedOrderAction(f,syncReq.requestId);
			return (CThostFtdcParkedOrderActionField)waitForRsp(syncReq);
		}finally{ removeSyncRequest(syncReq); }
	}
	/** 请求查询预埋撤单(SYNC ALL) */
	public CThostFtdcParkedOrderActionField[] SyncAllReqQryParkedOrderAction(CThostFtdcQryParkedOrderActionField f) throws JctpException {
		SyncRequest syncReq = createSyncRequest(true);
		try{
			ReqQryParkedOrderAction(f,syncReq.requestId);
			LinkedList list = waitForAllRsps(syncReq);
			return (CThostFtdcParkedOrderActionField[])list.toArray(new CThostFtdcParkedOrderActionField[list.size()]);
		}finally{ removeSyncRequest(syncReq); }
	}

	/** 请求查询交易通知 */
	public int ReqQryTradingNotice(CThostFtdcQryTradingNoticeField f) throws JctpException {
		int reqId = getNextRequestId();
		ReqQryTradingNotice(f,reqId);
		return reqId;
	}
	/** 请求查询交易通知 */
	public void ReqQryTradingNotice(CThostFtdcQryTradingNoticeField f, int nRequestID) throws JctpException {
		checkConnected();
		autoSleepReqQry();
		byte[] a1 = CThostFtdcQryTradingNoticeField.toBytes(f);
		int r = JctpApi.traderapiReq(this,nativeApiPtr, MethodId_TraderApi_ReqQryTradingNotice, a1, nRequestID);
		if ( r!=0 ) throw new JctpException(r);
	}
	/** 请求查询交易通知(SYNC) */
	public CThostFtdcTradingNoticeField SyncReqQryTradingNotice(CThostFtdcQryTradingNoticeField f) throws JctpException {
		SyncRequest syncReq = createSyncRequest(false);
		try{
			ReqQryTradingNotice(f,syncReq.requestId);
			return (CThostFtdcTradingNoticeField)waitForRsp(syncReq);
		}finally{ removeSyncRequest(syncReq); }
	}
	/** 请求查询交易通知(SYNC ALL) */
	public CThostFtdcTradingNoticeField[] SyncAllReqQryTradingNotice(CThostFtdcQryTradingNoticeField f) throws JctpException {
		SyncRequest syncReq = createSyncRequest(true);
		try{
			ReqQryTradingNotice(f,syncReq.requestId);
			LinkedList list = waitForAllRsps(syncReq);
			return (CThostFtdcTradingNoticeField[])list.toArray(new CThostFtdcTradingNoticeField[list.size()]);
		}finally{ removeSyncRequest(syncReq); }
	}

	/** 请求查询经纪公司交易参数 */
	public int ReqQryBrokerTradingParams(CThostFtdcQryBrokerTradingParamsField f) throws JctpException {
		int reqId = getNextRequestId();
		ReqQryBrokerTradingParams(f,reqId);
		return reqId;
	}
	/** 请求查询经纪公司交易参数 */
	public void ReqQryBrokerTradingParams(CThostFtdcQryBrokerTradingParamsField f, int nRequestID) throws JctpException {
		checkConnected();
		autoSleepReqQry();
		byte[] a1 = CThostFtdcQryBrokerTradingParamsField.toBytes(f);
		int r = JctpApi.traderapiReq(this,nativeApiPtr, MethodId_TraderApi_ReqQryBrokerTradingParams, a1, nRequestID);
		if ( r!=0 ) throw new JctpException(r);
	}
	/** 请求查询经纪公司交易参数(SYNC) */
	public CThostFtdcBrokerTradingParamsField SyncReqQryBrokerTradingParams(CThostFtdcQryBrokerTradingParamsField f) throws JctpException {
		SyncRequest syncReq = createSyncRequest(false);
		try{
			ReqQryBrokerTradingParams(f,syncReq.requestId);
			return (CThostFtdcBrokerTradingParamsField)waitForRsp(syncReq);
		}finally{ removeSyncRequest(syncReq); }
	}
	/** 请求查询经纪公司交易参数(SYNC ALL) */
	public CThostFtdcBrokerTradingParamsField[] SyncAllReqQryBrokerTradingParams(CThostFtdcQryBrokerTradingParamsField f) throws JctpException {
		SyncRequest syncReq = createSyncRequest(true);
		try{
			ReqQryBrokerTradingParams(f,syncReq.requestId);
			LinkedList list = waitForAllRsps(syncReq);
			return (CThostFtdcBrokerTradingParamsField[])list.toArray(new CThostFtdcBrokerTradingParamsField[list.size()]);
		}finally{ removeSyncRequest(syncReq); }
	}

	/** 请求查询经纪公司交易算法 */
	public int ReqQryBrokerTradingAlgos(CThostFtdcQryBrokerTradingAlgosField f) throws JctpException {
		int reqId = getNextRequestId();
		ReqQryBrokerTradingAlgos(f,reqId);
		return reqId;
	}
	/** 请求查询经纪公司交易算法 */
	public void ReqQryBrokerTradingAlgos(CThostFtdcQryBrokerTradingAlgosField f, int nRequestID) throws JctpException {
		checkConnected();
		autoSleepReqQry();
		byte[] a1 = CThostFtdcQryBrokerTradingAlgosField.toBytes(f);
		int r = JctpApi.traderapiReq(this,nativeApiPtr, MethodId_TraderApi_ReqQryBrokerTradingAlgos, a1, nRequestID);
		if ( r!=0 ) throw new JctpException(r);
	}
	/** 请求查询经纪公司交易算法(SYNC) */
	public CThostFtdcBrokerTradingAlgosField SyncReqQryBrokerTradingAlgos(CThostFtdcQryBrokerTradingAlgosField f) throws JctpException {
		SyncRequest syncReq = createSyncRequest(false);
		try{
			ReqQryBrokerTradingAlgos(f,syncReq.requestId);
			return (CThostFtdcBrokerTradingAlgosField)waitForRsp(syncReq);
		}finally{ removeSyncRequest(syncReq); }
	}
	/** 请求查询经纪公司交易算法(SYNC ALL) */
	public CThostFtdcBrokerTradingAlgosField[] SyncAllReqQryBrokerTradingAlgos(CThostFtdcQryBrokerTradingAlgosField f) throws JctpException {
		SyncRequest syncReq = createSyncRequest(true);
		try{
			ReqQryBrokerTradingAlgos(f,syncReq.requestId);
			LinkedList list = waitForAllRsps(syncReq);
			return (CThostFtdcBrokerTradingAlgosField[])list.toArray(new CThostFtdcBrokerTradingAlgosField[list.size()]);
		}finally{ removeSyncRequest(syncReq); }
	}

	/** 期货发起银行资金转期货请求 */
	public int ReqFromBankToFutureByFuture(CThostFtdcReqTransferField f) throws JctpException {
		int reqId = getNextRequestId();
		ReqFromBankToFutureByFuture(f,reqId);
		return reqId;
	}
	/** 期货发起银行资金转期货请求 */
	public void ReqFromBankToFutureByFuture(CThostFtdcReqTransferField f, int nRequestID) throws JctpException {
		checkConnected();
		byte[] a1 = CThostFtdcReqTransferField.toBytes(f);
		int r = JctpApi.traderapiReq(this,nativeApiPtr, MethodId_TraderApi_ReqFromBankToFutureByFuture, a1, nRequestID);
		if ( r!=0 ) throw new JctpException(r);
	}
	/** 期货发起银行资金转期货请求(SYNC) */
	public CThostFtdcReqTransferField SyncReqFromBankToFutureByFuture(CThostFtdcReqTransferField f) throws JctpException {
		SyncRequest syncReq = createSyncRequest(false);
		try{
			ReqFromBankToFutureByFuture(f,syncReq.requestId);
			return (CThostFtdcReqTransferField)waitForRsp(syncReq);
		}finally{ removeSyncRequest(syncReq); }
	}
	/** 期货发起银行资金转期货请求(SYNC ALL) */
	public CThostFtdcReqTransferField[] SyncAllReqFromBankToFutureByFuture(CThostFtdcReqTransferField f) throws JctpException {
		SyncRequest syncReq = createSyncRequest(true);
		try{
			ReqFromBankToFutureByFuture(f,syncReq.requestId);
			LinkedList list = waitForAllRsps(syncReq);
			return (CThostFtdcReqTransferField[])list.toArray(new CThostFtdcReqTransferField[list.size()]);
		}finally{ removeSyncRequest(syncReq); }
	}

	/** 期货发起期货资金转银行请求 */
	public int ReqFromFutureToBankByFuture(CThostFtdcReqTransferField f) throws JctpException {
		int reqId = getNextRequestId();
		ReqFromFutureToBankByFuture(f,reqId);
		return reqId;
	}
	/** 期货发起期货资金转银行请求 */
	public void ReqFromFutureToBankByFuture(CThostFtdcReqTransferField f, int nRequestID) throws JctpException {
		checkConnected();
		byte[] a1 = CThostFtdcReqTransferField.toBytes(f);
		int r = JctpApi.traderapiReq(this,nativeApiPtr, MethodId_TraderApi_ReqFromFutureToBankByFuture, a1, nRequestID);
		if ( r!=0 ) throw new JctpException(r);
	}
	/** 期货发起期货资金转银行请求(SYNC) */
	public CThostFtdcReqTransferField SyncReqFromFutureToBankByFuture(CThostFtdcReqTransferField f) throws JctpException {
		SyncRequest syncReq = createSyncRequest(false);
		try{
			ReqFromFutureToBankByFuture(f,syncReq.requestId);
			return (CThostFtdcReqTransferField)waitForRsp(syncReq);
		}finally{ removeSyncRequest(syncReq); }
	}
	/** 期货发起期货资金转银行请求(SYNC ALL) */
	public CThostFtdcReqTransferField[] SyncAllReqFromFutureToBankByFuture(CThostFtdcReqTransferField f) throws JctpException {
		SyncRequest syncReq = createSyncRequest(true);
		try{
			ReqFromFutureToBankByFuture(f,syncReq.requestId);
			LinkedList list = waitForAllRsps(syncReq);
			return (CThostFtdcReqTransferField[])list.toArray(new CThostFtdcReqTransferField[list.size()]);
		}finally{ removeSyncRequest(syncReq); }
	}

	/** 期货发起查询银行余额请求 */
	public int ReqQueryBankAccountMoneyByFuture(CThostFtdcReqQueryAccountField f) throws JctpException {
		int reqId = getNextRequestId();
		ReqQueryBankAccountMoneyByFuture(f,reqId);
		return reqId;
	}
	/** 期货发起查询银行余额请求 */
	public void ReqQueryBankAccountMoneyByFuture(CThostFtdcReqQueryAccountField f, int nRequestID) throws JctpException {
		checkConnected();
		byte[] a1 = CThostFtdcReqQueryAccountField.toBytes(f);
		int r = JctpApi.traderapiReq(this,nativeApiPtr, MethodId_TraderApi_ReqQueryBankAccountMoneyByFuture, a1, nRequestID);
		if ( r!=0 ) throw new JctpException(r);
	}
	/** 期货发起查询银行余额请求(SYNC) */
	public CThostFtdcReqQueryAccountField SyncReqQueryBankAccountMoneyByFuture(CThostFtdcReqQueryAccountField f) throws JctpException {
		SyncRequest syncReq = createSyncRequest(false);
		try{
			ReqQueryBankAccountMoneyByFuture(f,syncReq.requestId);
			return (CThostFtdcReqQueryAccountField)waitForRsp(syncReq);
		}finally{ removeSyncRequest(syncReq); }
	}
	/** 期货发起查询银行余额请求(SYNC ALL) */
	public CThostFtdcReqQueryAccountField[] SyncAllReqQueryBankAccountMoneyByFuture(CThostFtdcReqQueryAccountField f) throws JctpException {
		SyncRequest syncReq = createSyncRequest(true);
		try{
			ReqQueryBankAccountMoneyByFuture(f,syncReq.requestId);
			LinkedList list = waitForAllRsps(syncReq);
			return (CThostFtdcReqQueryAccountField[])list.toArray(new CThostFtdcReqQueryAccountField[list.size()]);
		}finally{ removeSyncRequest(syncReq); }
	}
	
    //---- OnRsp method
    
    private void OnRsp(int methodId, byte[] rspField, byte[] pRspInfo, int nRequestID, boolean bIsLast){
        if ( listener==null ) return;
         switch( methodId){
		case MethodId_TraderSpi_OnRspAuthenticate:
		{
			CThostFtdcRspAuthenticateField f = CThostFtdcRspAuthenticateField.fromBytes(rspField);
			CThostFtdcRspInfoField f2 = CThostFtdcRspInfoField.fromBytes(pRspInfo);
			notifyRequest(f, f2, nRequestID, bIsLast);
			listener.OnRspAuthenticate(f,f2,nRequestID,bIsLast);
			return;
		}
		case MethodId_TraderSpi_OnRspUserLogin:
		{
			CThostFtdcRspUserLoginField f = CThostFtdcRspUserLoginField.fromBytes(rspField);
			CThostFtdcRspInfoField f2 = CThostFtdcRspInfoField.fromBytes(pRspInfo);
			notifyRequest(f, f2, nRequestID, bIsLast);
			listener.OnRspUserLogin(f,f2,nRequestID,bIsLast);
			return;
		}
		case MethodId_TraderSpi_OnRspUserLogout:
		{
			CThostFtdcUserLogoutField f = CThostFtdcUserLogoutField.fromBytes(rspField);
			CThostFtdcRspInfoField f2 = CThostFtdcRspInfoField.fromBytes(pRspInfo);
			notifyRequest(f, f2, nRequestID, bIsLast);
			listener.OnRspUserLogout(f,f2,nRequestID,bIsLast);
			return;
		}
		case MethodId_TraderSpi_OnRspUserPasswordUpdate:
		{
			CThostFtdcUserPasswordUpdateField f = CThostFtdcUserPasswordUpdateField.fromBytes(rspField);
			CThostFtdcRspInfoField f2 = CThostFtdcRspInfoField.fromBytes(pRspInfo);
			notifyRequest(f, f2, nRequestID, bIsLast);
			listener.OnRspUserPasswordUpdate(f,f2,nRequestID,bIsLast);
			return;
		}
		case MethodId_TraderSpi_OnRspTradingAccountPasswordUpdate:
		{
			CThostFtdcTradingAccountPasswordUpdateField f = CThostFtdcTradingAccountPasswordUpdateField.fromBytes(rspField);
			CThostFtdcRspInfoField f2 = CThostFtdcRspInfoField.fromBytes(pRspInfo);
			notifyRequest(f, f2, nRequestID, bIsLast);
			listener.OnRspTradingAccountPasswordUpdate(f,f2,nRequestID,bIsLast);
			return;
		}
		case MethodId_TraderSpi_OnRspOrderInsert:
		{
			CThostFtdcInputOrderField f = CThostFtdcInputOrderField.fromBytes(rspField);
			CThostFtdcRspInfoField f2 = CThostFtdcRspInfoField.fromBytes(pRspInfo);
			notifyRequest(f, f2, nRequestID, bIsLast);
			listener.OnRspOrderInsert(f,f2,nRequestID,bIsLast);
			return;
		}
		case MethodId_TraderSpi_OnRspParkedOrderInsert:
		{
			CThostFtdcParkedOrderField f = CThostFtdcParkedOrderField.fromBytes(rspField);
			CThostFtdcRspInfoField f2 = CThostFtdcRspInfoField.fromBytes(pRspInfo);
			notifyRequest(f, f2, nRequestID, bIsLast);
			listener.OnRspParkedOrderInsert(f,f2,nRequestID,bIsLast);
			return;
		}
		case MethodId_TraderSpi_OnRspParkedOrderAction:
		{
			CThostFtdcParkedOrderActionField f = CThostFtdcParkedOrderActionField.fromBytes(rspField);
			CThostFtdcRspInfoField f2 = CThostFtdcRspInfoField.fromBytes(pRspInfo);
			notifyRequest(f, f2, nRequestID, bIsLast);
			listener.OnRspParkedOrderAction(f,f2,nRequestID,bIsLast);
			return;
		}
		case MethodId_TraderSpi_OnRspOrderAction:
		{
			CThostFtdcInputOrderActionField f = CThostFtdcInputOrderActionField.fromBytes(rspField);
			CThostFtdcRspInfoField f2 = CThostFtdcRspInfoField.fromBytes(pRspInfo);
			notifyRequest(f, f2, nRequestID, bIsLast);
			listener.OnRspOrderAction(f,f2,nRequestID,bIsLast);
			return;
		}
		case MethodId_TraderSpi_OnRspQueryMaxOrderVolume:
		{
			CThostFtdcQueryMaxOrderVolumeField f = CThostFtdcQueryMaxOrderVolumeField.fromBytes(rspField);
			CThostFtdcRspInfoField f2 = CThostFtdcRspInfoField.fromBytes(pRspInfo);
			notifyRequest(f, f2, nRequestID, bIsLast);
			listener.OnRspQueryMaxOrderVolume(f,f2,nRequestID,bIsLast);
			return;
		}
		case MethodId_TraderSpi_OnRspSettlementInfoConfirm:
		{
			CThostFtdcSettlementInfoConfirmField f = CThostFtdcSettlementInfoConfirmField.fromBytes(rspField);
			CThostFtdcRspInfoField f2 = CThostFtdcRspInfoField.fromBytes(pRspInfo);
			notifyRequest(f, f2, nRequestID, bIsLast);
			listener.OnRspSettlementInfoConfirm(f,f2,nRequestID,bIsLast);
			return;
		}
		case MethodId_TraderSpi_OnRspRemoveParkedOrder:
		{
			CThostFtdcRemoveParkedOrderField f = CThostFtdcRemoveParkedOrderField.fromBytes(rspField);
			CThostFtdcRspInfoField f2 = CThostFtdcRspInfoField.fromBytes(pRspInfo);
			notifyRequest(f, f2, nRequestID, bIsLast);
			listener.OnRspRemoveParkedOrder(f,f2,nRequestID,bIsLast);
			return;
		}
		case MethodId_TraderSpi_OnRspRemoveParkedOrderAction:
		{
			CThostFtdcRemoveParkedOrderActionField f = CThostFtdcRemoveParkedOrderActionField.fromBytes(rspField);
			CThostFtdcRspInfoField f2 = CThostFtdcRspInfoField.fromBytes(pRspInfo);
			notifyRequest(f, f2, nRequestID, bIsLast);
			listener.OnRspRemoveParkedOrderAction(f,f2,nRequestID,bIsLast);
			return;
		}
		case MethodId_TraderSpi_OnRspExecOrderInsert:
		{
			CThostFtdcInputExecOrderField f = CThostFtdcInputExecOrderField.fromBytes(rspField);
			CThostFtdcRspInfoField f2 = CThostFtdcRspInfoField.fromBytes(pRspInfo);
			notifyRequest(f, f2, nRequestID, bIsLast);
			listener.OnRspExecOrderInsert(f,f2,nRequestID,bIsLast);
			return;
		}
		case MethodId_TraderSpi_OnRspExecOrderAction:
		{
			CThostFtdcInputExecOrderActionField f = CThostFtdcInputExecOrderActionField.fromBytes(rspField);
			CThostFtdcRspInfoField f2 = CThostFtdcRspInfoField.fromBytes(pRspInfo);
			notifyRequest(f, f2, nRequestID, bIsLast);
			listener.OnRspExecOrderAction(f,f2,nRequestID,bIsLast);
			return;
		}
		case MethodId_TraderSpi_OnRspForQuoteInsert:
		{
			CThostFtdcInputForQuoteField f = CThostFtdcInputForQuoteField.fromBytes(rspField);
			CThostFtdcRspInfoField f2 = CThostFtdcRspInfoField.fromBytes(pRspInfo);
			notifyRequest(f, f2, nRequestID, bIsLast);
			listener.OnRspForQuoteInsert(f,f2,nRequestID,bIsLast);
			return;
		}
		case MethodId_TraderSpi_OnRspQuoteInsert:
		{
			CThostFtdcInputQuoteField f = CThostFtdcInputQuoteField.fromBytes(rspField);
			CThostFtdcRspInfoField f2 = CThostFtdcRspInfoField.fromBytes(pRspInfo);
			notifyRequest(f, f2, nRequestID, bIsLast);
			listener.OnRspQuoteInsert(f,f2,nRequestID,bIsLast);
			return;
		}
		case MethodId_TraderSpi_OnRspQuoteAction:
		{
			CThostFtdcInputQuoteActionField f = CThostFtdcInputQuoteActionField.fromBytes(rspField);
			CThostFtdcRspInfoField f2 = CThostFtdcRspInfoField.fromBytes(pRspInfo);
			notifyRequest(f, f2, nRequestID, bIsLast);
			listener.OnRspQuoteAction(f,f2,nRequestID,bIsLast);
			return;
		}
		case MethodId_TraderSpi_OnRspQryOrder:
		{
			CThostFtdcOrderField f = CThostFtdcOrderField.fromBytes(rspField);
			CThostFtdcRspInfoField f2 = CThostFtdcRspInfoField.fromBytes(pRspInfo);
			notifyRequest(f, f2, nRequestID, bIsLast);
			listener.OnRspQryOrder(f,f2,nRequestID,bIsLast);
			return;
		}
		case MethodId_TraderSpi_OnRspQryTrade:
		{
			CThostFtdcTradeField f = CThostFtdcTradeField.fromBytes(rspField);
			CThostFtdcRspInfoField f2 = CThostFtdcRspInfoField.fromBytes(pRspInfo);
			notifyRequest(f, f2, nRequestID, bIsLast);
			listener.OnRspQryTrade(f,f2,nRequestID,bIsLast);
			return;
		}
		case MethodId_TraderSpi_OnRspQryInvestorPosition:
		{
			CThostFtdcInvestorPositionField f = CThostFtdcInvestorPositionField.fromBytes(rspField);
			CThostFtdcRspInfoField f2 = CThostFtdcRspInfoField.fromBytes(pRspInfo);
			notifyRequest(f, f2, nRequestID, bIsLast);
			listener.OnRspQryInvestorPosition(f,f2,nRequestID,bIsLast);
			return;
		}
		case MethodId_TraderSpi_OnRspQryTradingAccount:
		{
			CThostFtdcTradingAccountField f = CThostFtdcTradingAccountField.fromBytes(rspField);
			CThostFtdcRspInfoField f2 = CThostFtdcRspInfoField.fromBytes(pRspInfo);
			notifyRequest(f, f2, nRequestID, bIsLast);
			listener.OnRspQryTradingAccount(f,f2,nRequestID,bIsLast);
			return;
		}
		case MethodId_TraderSpi_OnRspQryInvestor:
		{
			CThostFtdcInvestorField f = CThostFtdcInvestorField.fromBytes(rspField);
			CThostFtdcRspInfoField f2 = CThostFtdcRspInfoField.fromBytes(pRspInfo);
			notifyRequest(f, f2, nRequestID, bIsLast);
			listener.OnRspQryInvestor(f,f2,nRequestID,bIsLast);
			return;
		}
		case MethodId_TraderSpi_OnRspQryTradingCode:
		{
			CThostFtdcTradingCodeField f = CThostFtdcTradingCodeField.fromBytes(rspField);
			CThostFtdcRspInfoField f2 = CThostFtdcRspInfoField.fromBytes(pRspInfo);
			notifyRequest(f, f2, nRequestID, bIsLast);
			listener.OnRspQryTradingCode(f,f2,nRequestID,bIsLast);
			return;
		}
		case MethodId_TraderSpi_OnRspQryInstrumentMarginRate:
		{
			CThostFtdcInstrumentMarginRateField f = CThostFtdcInstrumentMarginRateField.fromBytes(rspField);
			CThostFtdcRspInfoField f2 = CThostFtdcRspInfoField.fromBytes(pRspInfo);
			notifyRequest(f, f2, nRequestID, bIsLast);
			listener.OnRspQryInstrumentMarginRate(f,f2,nRequestID,bIsLast);
			return;
		}
		case MethodId_TraderSpi_OnRspQryInstrumentCommissionRate:
		{
			CThostFtdcInstrumentCommissionRateField f = CThostFtdcInstrumentCommissionRateField.fromBytes(rspField);
			CThostFtdcRspInfoField f2 = CThostFtdcRspInfoField.fromBytes(pRspInfo);
			notifyRequest(f, f2, nRequestID, bIsLast);
			listener.OnRspQryInstrumentCommissionRate(f,f2,nRequestID,bIsLast);
			return;
		}
		case MethodId_TraderSpi_OnRspQryExchange:
		{
			CThostFtdcExchangeField f = CThostFtdcExchangeField.fromBytes(rspField);
			CThostFtdcRspInfoField f2 = CThostFtdcRspInfoField.fromBytes(pRspInfo);
			notifyRequest(f, f2, nRequestID, bIsLast);
			listener.OnRspQryExchange(f,f2,nRequestID,bIsLast);
			return;
		}
		case MethodId_TraderSpi_OnRspQryProduct:
		{
			CThostFtdcProductField f = CThostFtdcProductField.fromBytes(rspField);
			CThostFtdcRspInfoField f2 = CThostFtdcRspInfoField.fromBytes(pRspInfo);
			notifyRequest(f, f2, nRequestID, bIsLast);
			listener.OnRspQryProduct(f,f2,nRequestID,bIsLast);
			return;
		}
		case MethodId_TraderSpi_OnRspQryInstrument:
		{
			CThostFtdcInstrumentField f = CThostFtdcInstrumentField.fromBytes(rspField);
			CThostFtdcRspInfoField f2 = CThostFtdcRspInfoField.fromBytes(pRspInfo);
			notifyRequest(f, f2, nRequestID, bIsLast);
			listener.OnRspQryInstrument(f,f2,nRequestID,bIsLast);
			return;
		}
		case MethodId_TraderSpi_OnRspQryDepthMarketData:
		{
			CThostFtdcDepthMarketDataField f = CThostFtdcDepthMarketDataField.fromBytes(rspField);
			CThostFtdcRspInfoField f2 = CThostFtdcRspInfoField.fromBytes(pRspInfo);
			notifyRequest(f, f2, nRequestID, bIsLast);
			listener.OnRspQryDepthMarketData(f,f2,nRequestID,bIsLast);
			return;
		}
		case MethodId_TraderSpi_OnRspQrySettlementInfo:
		{
			CThostFtdcSettlementInfoField f = CThostFtdcSettlementInfoField.fromBytes(rspField);
			CThostFtdcRspInfoField f2 = CThostFtdcRspInfoField.fromBytes(pRspInfo);
			notifyRequest(f, f2, nRequestID, bIsLast);
			listener.OnRspQrySettlementInfo(f,f2,nRequestID,bIsLast);
			return;
		}
		case MethodId_TraderSpi_OnRspQryTransferBank:
		{
			CThostFtdcTransferBankField f = CThostFtdcTransferBankField.fromBytes(rspField);
			CThostFtdcRspInfoField f2 = CThostFtdcRspInfoField.fromBytes(pRspInfo);
			notifyRequest(f, f2, nRequestID, bIsLast);
			listener.OnRspQryTransferBank(f,f2,nRequestID,bIsLast);
			return;
		}
		case MethodId_TraderSpi_OnRspQryInvestorPositionDetail:
		{
			CThostFtdcInvestorPositionDetailField f = CThostFtdcInvestorPositionDetailField.fromBytes(rspField);
			CThostFtdcRspInfoField f2 = CThostFtdcRspInfoField.fromBytes(pRspInfo);
			notifyRequest(f, f2, nRequestID, bIsLast);
			listener.OnRspQryInvestorPositionDetail(f,f2,nRequestID,bIsLast);
			return;
		}
		case MethodId_TraderSpi_OnRspQryNotice:
		{
			CThostFtdcNoticeField f = CThostFtdcNoticeField.fromBytes(rspField);
			CThostFtdcRspInfoField f2 = CThostFtdcRspInfoField.fromBytes(pRspInfo);
			notifyRequest(f, f2, nRequestID, bIsLast);
			listener.OnRspQryNotice(f,f2,nRequestID,bIsLast);
			return;
		}
		case MethodId_TraderSpi_OnRspQrySettlementInfoConfirm:
		{
			CThostFtdcSettlementInfoConfirmField f = CThostFtdcSettlementInfoConfirmField.fromBytes(rspField);
			CThostFtdcRspInfoField f2 = CThostFtdcRspInfoField.fromBytes(pRspInfo);
			notifyRequest(f, f2, nRequestID, bIsLast);
			listener.OnRspQrySettlementInfoConfirm(f,f2,nRequestID,bIsLast);
			return;
		}
		case MethodId_TraderSpi_OnRspQryInvestorPositionCombineDetail:
		{
			CThostFtdcInvestorPositionCombineDetailField f = CThostFtdcInvestorPositionCombineDetailField.fromBytes(rspField);
			CThostFtdcRspInfoField f2 = CThostFtdcRspInfoField.fromBytes(pRspInfo);
			notifyRequest(f, f2, nRequestID, bIsLast);
			listener.OnRspQryInvestorPositionCombineDetail(f,f2,nRequestID,bIsLast);
			return;
		}
		case MethodId_TraderSpi_OnRspQryCFMMCTradingAccountKey:
		{
			CThostFtdcCFMMCTradingAccountKeyField f = CThostFtdcCFMMCTradingAccountKeyField.fromBytes(rspField);
			CThostFtdcRspInfoField f2 = CThostFtdcRspInfoField.fromBytes(pRspInfo);
			notifyRequest(f, f2, nRequestID, bIsLast);
			listener.OnRspQryCFMMCTradingAccountKey(f,f2,nRequestID,bIsLast);
			return;
		}
		case MethodId_TraderSpi_OnRspQryEWarrantOffset:
		{
			CThostFtdcEWarrantOffsetField f = CThostFtdcEWarrantOffsetField.fromBytes(rspField);
			CThostFtdcRspInfoField f2 = CThostFtdcRspInfoField.fromBytes(pRspInfo);
			notifyRequest(f, f2, nRequestID, bIsLast);
			listener.OnRspQryEWarrantOffset(f,f2,nRequestID,bIsLast);
			return;
		}
		case MethodId_TraderSpi_OnRspQryInvestorProductGroupMargin:
		{
			CThostFtdcInvestorProductGroupMarginField f = CThostFtdcInvestorProductGroupMarginField.fromBytes(rspField);
			CThostFtdcRspInfoField f2 = CThostFtdcRspInfoField.fromBytes(pRspInfo);
			notifyRequest(f, f2, nRequestID, bIsLast);
			listener.OnRspQryInvestorProductGroupMargin(f,f2,nRequestID,bIsLast);
			return;
		}
		case MethodId_TraderSpi_OnRspQryExchangeMarginRate:
		{
			CThostFtdcExchangeMarginRateField f = CThostFtdcExchangeMarginRateField.fromBytes(rspField);
			CThostFtdcRspInfoField f2 = CThostFtdcRspInfoField.fromBytes(pRspInfo);
			notifyRequest(f, f2, nRequestID, bIsLast);
			listener.OnRspQryExchangeMarginRate(f,f2,nRequestID,bIsLast);
			return;
		}
		case MethodId_TraderSpi_OnRspQryExchangeMarginRateAdjust:
		{
			CThostFtdcExchangeMarginRateAdjustField f = CThostFtdcExchangeMarginRateAdjustField.fromBytes(rspField);
			CThostFtdcRspInfoField f2 = CThostFtdcRspInfoField.fromBytes(pRspInfo);
			notifyRequest(f, f2, nRequestID, bIsLast);
			listener.OnRspQryExchangeMarginRateAdjust(f,f2,nRequestID,bIsLast);
			return;
		}
		case MethodId_TraderSpi_OnRspQryExchangeRate:
		{
			CThostFtdcExchangeRateField f = CThostFtdcExchangeRateField.fromBytes(rspField);
			CThostFtdcRspInfoField f2 = CThostFtdcRspInfoField.fromBytes(pRspInfo);
			notifyRequest(f, f2, nRequestID, bIsLast);
			listener.OnRspQryExchangeRate(f,f2,nRequestID,bIsLast);
			return;
		}
		case MethodId_TraderSpi_OnRspQrySecAgentACIDMap:
		{
			CThostFtdcSecAgentACIDMapField f = CThostFtdcSecAgentACIDMapField.fromBytes(rspField);
			CThostFtdcRspInfoField f2 = CThostFtdcRspInfoField.fromBytes(pRspInfo);
			notifyRequest(f, f2, nRequestID, bIsLast);
			listener.OnRspQrySecAgentACIDMap(f,f2,nRequestID,bIsLast);
			return;
		}
		case MethodId_TraderSpi_OnRspQryOptionInstrTradeCost:
		{
			CThostFtdcOptionInstrTradeCostField f = CThostFtdcOptionInstrTradeCostField.fromBytes(rspField);
			CThostFtdcRspInfoField f2 = CThostFtdcRspInfoField.fromBytes(pRspInfo);
			notifyRequest(f, f2, nRequestID, bIsLast);
			listener.OnRspQryOptionInstrTradeCost(f,f2,nRequestID,bIsLast);
			return;
		}
		case MethodId_TraderSpi_OnRspQryOptionInstrCommRate:
		{
			CThostFtdcOptionInstrCommRateField f = CThostFtdcOptionInstrCommRateField.fromBytes(rspField);
			CThostFtdcRspInfoField f2 = CThostFtdcRspInfoField.fromBytes(pRspInfo);
			notifyRequest(f, f2, nRequestID, bIsLast);
			listener.OnRspQryOptionInstrCommRate(f,f2,nRequestID,bIsLast);
			return;
		}
		case MethodId_TraderSpi_OnRspQryExecOrder:
		{
			CThostFtdcExecOrderField f = CThostFtdcExecOrderField.fromBytes(rspField);
			CThostFtdcRspInfoField f2 = CThostFtdcRspInfoField.fromBytes(pRspInfo);
			notifyRequest(f, f2, nRequestID, bIsLast);
			listener.OnRspQryExecOrder(f,f2,nRequestID,bIsLast);
			return;
		}
		case MethodId_TraderSpi_OnRspQryForQuote:
		{
			CThostFtdcForQuoteField f = CThostFtdcForQuoteField.fromBytes(rspField);
			CThostFtdcRspInfoField f2 = CThostFtdcRspInfoField.fromBytes(pRspInfo);
			notifyRequest(f, f2, nRequestID, bIsLast);
			listener.OnRspQryForQuote(f,f2,nRequestID,bIsLast);
			return;
		}
		case MethodId_TraderSpi_OnRspQryQuote:
		{
			CThostFtdcQuoteField f = CThostFtdcQuoteField.fromBytes(rspField);
			CThostFtdcRspInfoField f2 = CThostFtdcRspInfoField.fromBytes(pRspInfo);
			notifyRequest(f, f2, nRequestID, bIsLast);
			listener.OnRspQryQuote(f,f2,nRequestID,bIsLast);
			return;
		}
		case MethodId_TraderSpi_OnRspQryTransferSerial:
		{
			CThostFtdcTransferSerialField f = CThostFtdcTransferSerialField.fromBytes(rspField);
			CThostFtdcRspInfoField f2 = CThostFtdcRspInfoField.fromBytes(pRspInfo);
			notifyRequest(f, f2, nRequestID, bIsLast);
			listener.OnRspQryTransferSerial(f,f2,nRequestID,bIsLast);
			return;
		}
		case MethodId_TraderSpi_OnRspQryAccountregister:
		{
			CThostFtdcAccountregisterField f = CThostFtdcAccountregisterField.fromBytes(rspField);
			CThostFtdcRspInfoField f2 = CThostFtdcRspInfoField.fromBytes(pRspInfo);
			notifyRequest(f, f2, nRequestID, bIsLast);
			listener.OnRspQryAccountregister(f,f2,nRequestID,bIsLast);
			return;
		}
		case MethodId_TraderSpi_OnRspError:
		{
			CThostFtdcRspInfoField f = CThostFtdcRspInfoField.fromBytes(rspField);
			notifyRequest(null, f, nRequestID, bIsLast);
			listener.OnRspError(f,nRequestID,bIsLast);
			return;
		}
		case MethodId_TraderSpi_OnRtnOrder:
		{
			CThostFtdcOrderField f = CThostFtdcOrderField.fromBytes(rspField);
			listener.OnRtnOrder(f);
			return;
		}
		case MethodId_TraderSpi_OnRtnTrade:
		{
			CThostFtdcTradeField f = CThostFtdcTradeField.fromBytes(rspField);
			listener.OnRtnTrade(f);
			return;
		}
		case MethodId_TraderSpi_OnErrRtnOrderInsert:
		{
			CThostFtdcInputOrderField f = CThostFtdcInputOrderField.fromBytes(rspField);
			CThostFtdcRspInfoField f2 = CThostFtdcRspInfoField.fromBytes(pRspInfo);
			listener.OnErrRtnOrderInsert(f,f2);
			return;
		}
		case MethodId_TraderSpi_OnErrRtnOrderAction:
		{
			CThostFtdcOrderActionField f = CThostFtdcOrderActionField.fromBytes(rspField);
			CThostFtdcRspInfoField f2 = CThostFtdcRspInfoField.fromBytes(pRspInfo);
			listener.OnErrRtnOrderAction(f,f2);
			return;
		}
		case MethodId_TraderSpi_OnRtnInstrumentStatus:
		{
			CThostFtdcInstrumentStatusField f = CThostFtdcInstrumentStatusField.fromBytes(rspField);
			listener.OnRtnInstrumentStatus(f);
			return;
		}
		case MethodId_TraderSpi_OnRtnTradingNotice:
		{
			CThostFtdcTradingNoticeInfoField f = CThostFtdcTradingNoticeInfoField.fromBytes(rspField);
			listener.OnRtnTradingNotice(f);
			return;
		}
		case MethodId_TraderSpi_OnRtnErrorConditionalOrder:
		{
			CThostFtdcErrorConditionalOrderField f = CThostFtdcErrorConditionalOrderField.fromBytes(rspField);
			listener.OnRtnErrorConditionalOrder(f);
			return;
		}
		case MethodId_TraderSpi_OnRtnExecOrder:
		{
			CThostFtdcExecOrderField f = CThostFtdcExecOrderField.fromBytes(rspField);
			listener.OnRtnExecOrder(f);
			return;
		}
		case MethodId_TraderSpi_OnErrRtnExecOrderInsert:
		{
			CThostFtdcInputExecOrderField f = CThostFtdcInputExecOrderField.fromBytes(rspField);
			CThostFtdcRspInfoField f2 = CThostFtdcRspInfoField.fromBytes(pRspInfo);
			listener.OnErrRtnExecOrderInsert(f,f2);
			return;
		}
		case MethodId_TraderSpi_OnErrRtnExecOrderAction:
		{
			CThostFtdcExecOrderActionField f = CThostFtdcExecOrderActionField.fromBytes(rspField);
			CThostFtdcRspInfoField f2 = CThostFtdcRspInfoField.fromBytes(pRspInfo);
			listener.OnErrRtnExecOrderAction(f,f2);
			return;
		}
		case MethodId_TraderSpi_OnErrRtnForQuoteInsert:
		{
			CThostFtdcInputExecOrderField f = CThostFtdcInputExecOrderField.fromBytes(rspField);
			CThostFtdcRspInfoField f2 = CThostFtdcRspInfoField.fromBytes(pRspInfo);
			listener.OnErrRtnForQuoteInsert(f,f2);
			return;
		}
		case MethodId_TraderSpi_OnRtnQuote:
		{
			CThostFtdcQuoteField f = CThostFtdcQuoteField.fromBytes(rspField);
			listener.OnRtnQuote(f);
			return;
		}
		case MethodId_TraderSpi_OnErrRtnQuoteInsert:
		{
			CThostFtdcInputQuoteField f = CThostFtdcInputQuoteField.fromBytes(rspField);
			CThostFtdcRspInfoField f2 = CThostFtdcRspInfoField.fromBytes(pRspInfo);
			listener.OnErrRtnQuoteInsert(f,f2);
			return;
		}
		case MethodId_TraderSpi_OnErrRtnQuoteAction:
		{
			CThostFtdcQuoteActionField f = CThostFtdcQuoteActionField.fromBytes(rspField);
			CThostFtdcRspInfoField f2 = CThostFtdcRspInfoField.fromBytes(pRspInfo);
			listener.OnErrRtnQuoteAction(f,f2);
			return;
		}
		case MethodId_TraderSpi_OnRtnForQuoteRsp:
		{
			CThostFtdcForQuoteRspField f = CThostFtdcForQuoteRspField.fromBytes(rspField);
			listener.OnRtnForQuoteRsp(f);
			return;
		}
		case MethodId_TraderSpi_OnRspQryContractBank:
		{
			CThostFtdcContractBankField f = CThostFtdcContractBankField.fromBytes(rspField);
			CThostFtdcRspInfoField f2 = CThostFtdcRspInfoField.fromBytes(pRspInfo);
			notifyRequest(f, f2, nRequestID, bIsLast);
			listener.OnRspQryContractBank(f,f2,nRequestID,bIsLast);
			return;
		}
		case MethodId_TraderSpi_OnRspQryParkedOrder:
		{
			CThostFtdcParkedOrderField f = CThostFtdcParkedOrderField.fromBytes(rspField);
			CThostFtdcRspInfoField f2 = CThostFtdcRspInfoField.fromBytes(pRspInfo);
			notifyRequest(f, f2, nRequestID, bIsLast);
			listener.OnRspQryParkedOrder(f,f2,nRequestID,bIsLast);
			return;
		}
		case MethodId_TraderSpi_OnRspQryParkedOrderAction:
		{
			CThostFtdcParkedOrderActionField f = CThostFtdcParkedOrderActionField.fromBytes(rspField);
			CThostFtdcRspInfoField f2 = CThostFtdcRspInfoField.fromBytes(pRspInfo);
			notifyRequest(f, f2, nRequestID, bIsLast);
			listener.OnRspQryParkedOrderAction(f,f2,nRequestID,bIsLast);
			return;
		}
		case MethodId_TraderSpi_OnRspQryTradingNotice:
		{
			CThostFtdcTradingNoticeField f = CThostFtdcTradingNoticeField.fromBytes(rspField);
			CThostFtdcRspInfoField f2 = CThostFtdcRspInfoField.fromBytes(pRspInfo);
			notifyRequest(f, f2, nRequestID, bIsLast);
			listener.OnRspQryTradingNotice(f,f2,nRequestID,bIsLast);
			return;
		}
		case MethodId_TraderSpi_OnRspQryBrokerTradingParams:
		{
			CThostFtdcBrokerTradingParamsField f = CThostFtdcBrokerTradingParamsField.fromBytes(rspField);
			CThostFtdcRspInfoField f2 = CThostFtdcRspInfoField.fromBytes(pRspInfo);
			notifyRequest(f, f2, nRequestID, bIsLast);
			listener.OnRspQryBrokerTradingParams(f,f2,nRequestID,bIsLast);
			return;
		}
		case MethodId_TraderSpi_OnRspQryBrokerTradingAlgos:
		{
			CThostFtdcBrokerTradingAlgosField f = CThostFtdcBrokerTradingAlgosField.fromBytes(rspField);
			CThostFtdcRspInfoField f2 = CThostFtdcRspInfoField.fromBytes(pRspInfo);
			notifyRequest(f, f2, nRequestID, bIsLast);
			listener.OnRspQryBrokerTradingAlgos(f,f2,nRequestID,bIsLast);
			return;
		}
		case MethodId_TraderSpi_OnRtnFromBankToFutureByBank:
		{
			CThostFtdcRspTransferField f = CThostFtdcRspTransferField.fromBytes(rspField);
			listener.OnRtnFromBankToFutureByBank(f);
			return;
		}
		case MethodId_TraderSpi_OnRtnFromFutureToBankByBank:
		{
			CThostFtdcRspTransferField f = CThostFtdcRspTransferField.fromBytes(rspField);
			listener.OnRtnFromFutureToBankByBank(f);
			return;
		}
		case MethodId_TraderSpi_OnRtnRepealFromBankToFutureByBank:
		{
			CThostFtdcRspRepealField f = CThostFtdcRspRepealField.fromBytes(rspField);
			listener.OnRtnRepealFromBankToFutureByBank(f);
			return;
		}
		case MethodId_TraderSpi_OnRtnRepealFromFutureToBankByBank:
		{
			CThostFtdcRspRepealField f = CThostFtdcRspRepealField.fromBytes(rspField);
			listener.OnRtnRepealFromFutureToBankByBank(f);
			return;
		}
		case MethodId_TraderSpi_OnRtnFromBankToFutureByFuture:
		{
			CThostFtdcRspTransferField f = CThostFtdcRspTransferField.fromBytes(rspField);
			listener.OnRtnFromBankToFutureByFuture(f);
			return;
		}
		case MethodId_TraderSpi_OnRtnFromFutureToBankByFuture:
		{
			CThostFtdcRspTransferField f = CThostFtdcRspTransferField.fromBytes(rspField);
			listener.OnRtnFromFutureToBankByFuture(f);
			return;
		}
		case MethodId_TraderSpi_OnRtnRepealFromBankToFutureByFutureManual:
		{
			CThostFtdcRspRepealField f = CThostFtdcRspRepealField.fromBytes(rspField);
			listener.OnRtnRepealFromBankToFutureByFutureManual(f);
			return;
		}
		case MethodId_TraderSpi_OnRtnRepealFromFutureToBankByFutureManual:
		{
			CThostFtdcRspRepealField f = CThostFtdcRspRepealField.fromBytes(rspField);
			listener.OnRtnRepealFromFutureToBankByFutureManual(f);
			return;
		}
		case MethodId_TraderSpi_OnRtnQueryBankBalanceByFuture:
		{
			CThostFtdcNotifyQueryAccountField f = CThostFtdcNotifyQueryAccountField.fromBytes(rspField);
			listener.OnRtnQueryBankBalanceByFuture(f);
			return;
		}
		case MethodId_TraderSpi_OnErrRtnBankToFutureByFuture:
		{
			CThostFtdcReqTransferField f = CThostFtdcReqTransferField.fromBytes(rspField);
			CThostFtdcRspInfoField f2 = CThostFtdcRspInfoField.fromBytes(pRspInfo);
			listener.OnErrRtnBankToFutureByFuture(f,f2);
			return;
		}
		case MethodId_TraderSpi_OnErrRtnFutureToBankByFuture:
		{
			CThostFtdcReqTransferField f = CThostFtdcReqTransferField.fromBytes(rspField);
			CThostFtdcRspInfoField f2 = CThostFtdcRspInfoField.fromBytes(pRspInfo);
			listener.OnErrRtnFutureToBankByFuture(f,f2);
			return;
		}
		case MethodId_TraderSpi_OnErrRtnRepealBankToFutureByFutureManual:
		{
			CThostFtdcReqRepealField f = CThostFtdcReqRepealField.fromBytes(rspField);
			CThostFtdcRspInfoField f2 = CThostFtdcRspInfoField.fromBytes(pRspInfo);
			listener.OnErrRtnRepealBankToFutureByFutureManual(f,f2);
			return;
		}
		case MethodId_TraderSpi_OnErrRtnRepealFutureToBankByFutureManual:
		{
			CThostFtdcReqRepealField f = CThostFtdcReqRepealField.fromBytes(rspField);
			CThostFtdcRspInfoField f2 = CThostFtdcRspInfoField.fromBytes(pRspInfo);
			listener.OnErrRtnRepealFutureToBankByFutureManual(f,f2);
			return;
		}
		case MethodId_TraderSpi_OnErrRtnQueryBankBalanceByFuture:
		{
			CThostFtdcReqQueryAccountField f = CThostFtdcReqQueryAccountField.fromBytes(rspField);
			CThostFtdcRspInfoField f2 = CThostFtdcRspInfoField.fromBytes(pRspInfo);
			listener.OnErrRtnQueryBankBalanceByFuture(f,f2);
			return;
		}
		case MethodId_TraderSpi_OnRtnRepealFromBankToFutureByFuture:
		{
			CThostFtdcRspRepealField f = CThostFtdcRspRepealField.fromBytes(rspField);
			listener.OnRtnRepealFromBankToFutureByFuture(f);
			return;
		}
		case MethodId_TraderSpi_OnRtnRepealFromFutureToBankByFuture:
		{
			CThostFtdcRspRepealField f = CThostFtdcRspRepealField.fromBytes(rspField);
			listener.OnRtnRepealFromFutureToBankByFuture(f);
			return;
		}
		case MethodId_TraderSpi_OnRspFromBankToFutureByFuture:
		{
			CThostFtdcReqTransferField f = CThostFtdcReqTransferField.fromBytes(rspField);
			CThostFtdcRspInfoField f2 = CThostFtdcRspInfoField.fromBytes(pRspInfo);
			notifyRequest(f, f2, nRequestID, bIsLast);
			listener.OnRspFromBankToFutureByFuture(f,f2,nRequestID,bIsLast);
			return;
		}
		case MethodId_TraderSpi_OnRspFromFutureToBankByFuture:
		{
			CThostFtdcReqTransferField f = CThostFtdcReqTransferField.fromBytes(rspField);
			CThostFtdcRspInfoField f2 = CThostFtdcRspInfoField.fromBytes(pRspInfo);
			notifyRequest(f, f2, nRequestID, bIsLast);
			listener.OnRspFromFutureToBankByFuture(f,f2,nRequestID,bIsLast);
			return;
		}
		case MethodId_TraderSpi_OnRspQueryBankAccountMoneyByFuture:
		{
			CThostFtdcReqQueryAccountField f = CThostFtdcReqQueryAccountField.fromBytes(rspField);
			CThostFtdcRspInfoField f2 = CThostFtdcRspInfoField.fromBytes(pRspInfo);
			notifyRequest(f, f2, nRequestID, bIsLast);
			listener.OnRspQueryBankAccountMoneyByFuture(f,f2,nRequestID,bIsLast);
			return;
		}
		case MethodId_TraderSpi_OnRtnOpenAccountByBank:
		{
			CThostFtdcOpenAccountField f = CThostFtdcOpenAccountField.fromBytes(rspField);
			listener.OnRtnOpenAccountByBank(f);
			return;
		}
		case MethodId_TraderSpi_OnRtnCancelAccountByBank:
		{
			CThostFtdcCancelAccountField f = CThostFtdcCancelAccountField.fromBytes(rspField);
			listener.OnRtnCancelAccountByBank(f);
			return;
		}
		case MethodId_TraderSpi_OnRtnChangeAccountByBank:
		{
			CThostFtdcChangeAccountField f = CThostFtdcChangeAccountField.fromBytes(rspField);
			listener.OnRtnChangeAccountByBank(f);
			return;
		}

        default: 
        throw new RuntimeException("OnRsp method can't dispatch methodId: "+methodId);
        }
    }

    
    //add in 2014-10-24
    public void setLogin(boolean status){
    	this.login = status;
    }

	public String getConfirmDay() {
		return confirmDay;
	}

	public void setConfirmDay(String confirmDay) {
		this.confirmDay = confirmDay;
	}
    
    
}
