package net.jctp;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.concurrent.atomic.AtomicInteger;

import net.jctp.JctpException;
import net.jctp.MdApi;

public class JctpApi {

    static final Charset fieldCharset = Charset.forName(System.getProperty("jctp.encoding", "GBK"));
    
    static native void registerNatives(); 
    static{
        System.loadLibrary("jctp");
        registerNatives();
    }
    
	protected static final int MethodId_TraderSpi_OnRspAuthenticate=1;
	protected static final int MethodId_TraderSpi_OnRspUserLogin=2;
	protected static final int MethodId_TraderSpi_OnRspUserLogout=3;
	protected static final int MethodId_TraderSpi_OnRspUserPasswordUpdate=4;
	protected static final int MethodId_TraderSpi_OnRspTradingAccountPasswordUpdate=5;
	protected static final int MethodId_TraderSpi_OnRspOrderInsert=6;
	protected static final int MethodId_TraderSpi_OnRspParkedOrderInsert=7;
	protected static final int MethodId_TraderSpi_OnRspParkedOrderAction=8;
	protected static final int MethodId_TraderSpi_OnRspOrderAction=9;
	protected static final int MethodId_TraderSpi_OnRspQueryMaxOrderVolume=10;
	protected static final int MethodId_TraderSpi_OnRspSettlementInfoConfirm=11;
	protected static final int MethodId_TraderSpi_OnRspRemoveParkedOrder=12;
	protected static final int MethodId_TraderSpi_OnRspRemoveParkedOrderAction=13;
	protected static final int MethodId_TraderSpi_OnRspExecOrderInsert=14;
	protected static final int MethodId_TraderSpi_OnRspExecOrderAction=15;
	protected static final int MethodId_TraderSpi_OnRspForQuoteInsert=16;
	protected static final int MethodId_TraderSpi_OnRspQuoteInsert=17;
	protected static final int MethodId_TraderSpi_OnRspQuoteAction=18;
	protected static final int MethodId_TraderSpi_OnRspQryOrder=19;
	protected static final int MethodId_TraderSpi_OnRspQryTrade=20;
	protected static final int MethodId_TraderSpi_OnRspQryInvestorPosition=21;
	protected static final int MethodId_TraderSpi_OnRspQryTradingAccount=22;
	protected static final int MethodId_TraderSpi_OnRspQryInvestor=23;
	protected static final int MethodId_TraderSpi_OnRspQryTradingCode=24;
	protected static final int MethodId_TraderSpi_OnRspQryInstrumentMarginRate=25;
	protected static final int MethodId_TraderSpi_OnRspQryInstrumentCommissionRate=26;
	protected static final int MethodId_TraderSpi_OnRspQryExchange=27;
	protected static final int MethodId_TraderSpi_OnRspQryProduct=28;
	protected static final int MethodId_TraderSpi_OnRspQryInstrument=29;
	protected static final int MethodId_TraderSpi_OnRspQryDepthMarketData=30;
	protected static final int MethodId_TraderSpi_OnRspQrySettlementInfo=31;
	protected static final int MethodId_TraderSpi_OnRspQryTransferBank=32;
	protected static final int MethodId_TraderSpi_OnRspQryInvestorPositionDetail=33;
	protected static final int MethodId_TraderSpi_OnRspQryNotice=34;
	protected static final int MethodId_TraderSpi_OnRspQrySettlementInfoConfirm=35;
	protected static final int MethodId_TraderSpi_OnRspQryInvestorPositionCombineDetail=36;
	protected static final int MethodId_TraderSpi_OnRspQryCFMMCTradingAccountKey=37;
	protected static final int MethodId_TraderSpi_OnRspQryEWarrantOffset=38;
	protected static final int MethodId_TraderSpi_OnRspQryInvestorProductGroupMargin=39;
	protected static final int MethodId_TraderSpi_OnRspQryExchangeMarginRate=40;
	protected static final int MethodId_TraderSpi_OnRspQryExchangeMarginRateAdjust=41;
	protected static final int MethodId_TraderSpi_OnRspQryExchangeRate=42;
	protected static final int MethodId_TraderSpi_OnRspQrySecAgentACIDMap=43;
	protected static final int MethodId_TraderSpi_OnRspQryOptionInstrTradeCost=44;
	protected static final int MethodId_TraderSpi_OnRspQryOptionInstrCommRate=45;
	protected static final int MethodId_TraderSpi_OnRspQryExecOrder=46;
	protected static final int MethodId_TraderSpi_OnRspQryForQuote=47;
	protected static final int MethodId_TraderSpi_OnRspQryQuote=48;
	protected static final int MethodId_TraderSpi_OnRspQryTransferSerial=49;
	protected static final int MethodId_TraderSpi_OnRspQryAccountregister=50;
	protected static final int MethodId_TraderSpi_OnRspError=51;
	protected static final int MethodId_TraderSpi_OnRtnOrder=52;
	protected static final int MethodId_TraderSpi_OnRtnTrade=53;
	protected static final int MethodId_TraderSpi_OnErrRtnOrderInsert=54;
	protected static final int MethodId_TraderSpi_OnErrRtnOrderAction=55;
	protected static final int MethodId_TraderSpi_OnRtnInstrumentStatus=56;
	protected static final int MethodId_TraderSpi_OnRtnTradingNotice=57;
	protected static final int MethodId_TraderSpi_OnRtnErrorConditionalOrder=58;
	protected static final int MethodId_TraderSpi_OnRtnExecOrder=59;
	protected static final int MethodId_TraderSpi_OnErrRtnExecOrderInsert=60;
	protected static final int MethodId_TraderSpi_OnErrRtnExecOrderAction=61;
	protected static final int MethodId_TraderSpi_OnErrRtnForQuoteInsert=62;
	protected static final int MethodId_TraderSpi_OnRtnQuote=63;
	protected static final int MethodId_TraderSpi_OnErrRtnQuoteInsert=64;
	protected static final int MethodId_TraderSpi_OnErrRtnQuoteAction=65;
	protected static final int MethodId_TraderSpi_OnRtnForQuoteRsp=66;
	protected static final int MethodId_TraderSpi_OnRspQryContractBank=67;
	protected static final int MethodId_TraderSpi_OnRspQryParkedOrder=68;
	protected static final int MethodId_TraderSpi_OnRspQryParkedOrderAction=69;
	protected static final int MethodId_TraderSpi_OnRspQryTradingNotice=70;
	protected static final int MethodId_TraderSpi_OnRspQryBrokerTradingParams=71;
	protected static final int MethodId_TraderSpi_OnRspQryBrokerTradingAlgos=72;
	protected static final int MethodId_TraderSpi_OnRtnFromBankToFutureByBank=73;
	protected static final int MethodId_TraderSpi_OnRtnFromFutureToBankByBank=74;
	protected static final int MethodId_TraderSpi_OnRtnRepealFromBankToFutureByBank=75;
	protected static final int MethodId_TraderSpi_OnRtnRepealFromFutureToBankByBank=76;
	protected static final int MethodId_TraderSpi_OnRtnFromBankToFutureByFuture=77;
	protected static final int MethodId_TraderSpi_OnRtnFromFutureToBankByFuture=78;
	protected static final int MethodId_TraderSpi_OnRtnRepealFromBankToFutureByFutureManual=79;
	protected static final int MethodId_TraderSpi_OnRtnRepealFromFutureToBankByFutureManual=80;
	protected static final int MethodId_TraderSpi_OnRtnQueryBankBalanceByFuture=81;
	protected static final int MethodId_TraderSpi_OnErrRtnBankToFutureByFuture=82;
	protected static final int MethodId_TraderSpi_OnErrRtnFutureToBankByFuture=83;
	protected static final int MethodId_TraderSpi_OnErrRtnRepealBankToFutureByFutureManual=84;
	protected static final int MethodId_TraderSpi_OnErrRtnRepealFutureToBankByFutureManual=85;
	protected static final int MethodId_TraderSpi_OnErrRtnQueryBankBalanceByFuture=86;
	protected static final int MethodId_TraderSpi_OnRtnRepealFromBankToFutureByFuture=87;
	protected static final int MethodId_TraderSpi_OnRtnRepealFromFutureToBankByFuture=88;
	protected static final int MethodId_TraderSpi_OnRspFromBankToFutureByFuture=89;
	protected static final int MethodId_TraderSpi_OnRspFromFutureToBankByFuture=90;
	protected static final int MethodId_TraderSpi_OnRspQueryBankAccountMoneyByFuture=91;
	protected static final int MethodId_TraderSpi_OnRtnOpenAccountByBank=92;
	protected static final int MethodId_TraderSpi_OnRtnCancelAccountByBank=93;
	protected static final int MethodId_TraderSpi_OnRtnChangeAccountByBank=94;
	protected static final int MethodId_TraderApi_ReqAuthenticate=95;
	protected static final int MethodId_TraderApi_ReqUserLogin=96;
	protected static final int MethodId_TraderApi_ReqUserLogout=97;
	protected static final int MethodId_TraderApi_ReqUserPasswordUpdate=98;
	protected static final int MethodId_TraderApi_ReqTradingAccountPasswordUpdate=99;
	protected static final int MethodId_TraderApi_ReqOrderInsert=100;
	protected static final int MethodId_TraderApi_ReqParkedOrderInsert=101;
	protected static final int MethodId_TraderApi_ReqParkedOrderAction=102;
	protected static final int MethodId_TraderApi_ReqOrderAction=103;
	protected static final int MethodId_TraderApi_ReqQueryMaxOrderVolume=104;
	protected static final int MethodId_TraderApi_ReqSettlementInfoConfirm=105;
	protected static final int MethodId_TraderApi_ReqRemoveParkedOrder=106;
	protected static final int MethodId_TraderApi_ReqRemoveParkedOrderAction=107;
	protected static final int MethodId_TraderApi_ReqExecOrderInsert=108;
	protected static final int MethodId_TraderApi_ReqExecOrderAction=109;
	protected static final int MethodId_TraderApi_ReqForQuoteInsert=110;
	protected static final int MethodId_TraderApi_ReqQuoteInsert=111;
	protected static final int MethodId_TraderApi_ReqQuoteAction=112;
	protected static final int MethodId_TraderApi_ReqQryOrder=113;
	protected static final int MethodId_TraderApi_ReqQryTrade=114;
	protected static final int MethodId_TraderApi_ReqQryInvestorPosition=115;
	protected static final int MethodId_TraderApi_ReqQryTradingAccount=116;
	protected static final int MethodId_TraderApi_ReqQryInvestor=117;
	protected static final int MethodId_TraderApi_ReqQryTradingCode=118;
	protected static final int MethodId_TraderApi_ReqQryInstrumentMarginRate=119;
	protected static final int MethodId_TraderApi_ReqQryInstrumentCommissionRate=120;
	protected static final int MethodId_TraderApi_ReqQryExchange=121;
	protected static final int MethodId_TraderApi_ReqQryProduct=122;
	protected static final int MethodId_TraderApi_ReqQryInstrument=123;
	protected static final int MethodId_TraderApi_ReqQryDepthMarketData=124;
	protected static final int MethodId_TraderApi_ReqQrySettlementInfo=125;
	protected static final int MethodId_TraderApi_ReqQryTransferBank=126;
	protected static final int MethodId_TraderApi_ReqQryInvestorPositionDetail=127;
	protected static final int MethodId_TraderApi_ReqQryNotice=128;
	protected static final int MethodId_TraderApi_ReqQrySettlementInfoConfirm=129;
	protected static final int MethodId_TraderApi_ReqQryInvestorPositionCombineDetail=130;
	protected static final int MethodId_TraderApi_ReqQryCFMMCTradingAccountKey=131;
	protected static final int MethodId_TraderApi_ReqQryEWarrantOffset=132;
	protected static final int MethodId_TraderApi_ReqQryInvestorProductGroupMargin=133;
	protected static final int MethodId_TraderApi_ReqQryExchangeMarginRate=134;
	protected static final int MethodId_TraderApi_ReqQryExchangeMarginRateAdjust=135;
	protected static final int MethodId_TraderApi_ReqQryExchangeRate=136;
	protected static final int MethodId_TraderApi_ReqQrySecAgentACIDMap=137;
	protected static final int MethodId_TraderApi_ReqQryOptionInstrTradeCost=138;
	protected static final int MethodId_TraderApi_ReqQryOptionInstrCommRate=139;
	protected static final int MethodId_TraderApi_ReqQryExecOrder=140;
	protected static final int MethodId_TraderApi_ReqQryForQuote=141;
	protected static final int MethodId_TraderApi_ReqQryQuote=142;
	protected static final int MethodId_TraderApi_ReqQryTransferSerial=143;
	protected static final int MethodId_TraderApi_ReqQryAccountregister=144;
	protected static final int MethodId_TraderApi_ReqQryContractBank=145;
	protected static final int MethodId_TraderApi_ReqQryParkedOrder=146;
	protected static final int MethodId_TraderApi_ReqQryParkedOrderAction=147;
	protected static final int MethodId_TraderApi_ReqQryTradingNotice=148;
	protected static final int MethodId_TraderApi_ReqQryBrokerTradingParams=149;
	protected static final int MethodId_TraderApi_ReqQryBrokerTradingAlgos=150;
	protected static final int MethodId_TraderApi_ReqFromBankToFutureByFuture=151;
	protected static final int MethodId_TraderApi_ReqFromFutureToBankByFuture=152;
	protected static final int MethodId_TraderApi_ReqQueryBankAccountMoneyByFuture=153;
    
    protected long nativeApiPtr;
    protected volatile boolean connected;
    protected volatile boolean login;
    protected AtomicInteger nextRequestId = new AtomicInteger(-1);
    
    protected void checkConnected()
        throws JctpException
    {
        if ( nativeApiPtr==0 || !connected )
            throw new JctpException(JctpException.ERROR_NOT_CONNECTED);
    }
    
    public boolean isConnected(){
        return connected;
    }
    
    public boolean isLogin(){
        return login;
    }
    
    protected int getNextRequestId(){
        return nextRequestId.incrementAndGet();
    }
    
    public int getLastRequstId(){
        return nextRequestId.get();
    }

    protected static void strcopy(ByteBuffer data, String field, int offset, int sizeof ){
        if ( field==null )
            return;
        byte[] b = field.getBytes(fieldCharset);
        int lengthToCopy = b.length;
        if ( b.length>sizeof )
            lengthToCopy = sizeof;
        data.position(offset);
        data.put(b,0,lengthToCopy);
    }
    
    protected static byte[] str2c(String str){
        if ( str==null )
            return null;
        byte[] d = str.getBytes(fieldCharset);
        byte[] r = new byte[d.length+1];
        System.arraycopy(d, 0, r, 0, d.length);
        return r;
    }
    
    protected static byte[] strs2c(String[] strs){
        if ( strs==null || strs.length==0 ) 
            return null;
        byte[][] bytes = new byte[strs.length][];
        for(int i=0;i<strs.length;i++)
            bytes[i] = strs[i].getBytes(fieldCharset);
        int len = 0;
        for(int i=0;i<strs.length;i++){
            len += (bytes[i].length+1);
        }
        byte[] block = new byte[len+2];
        int blockOfs = 0;
        for(int i=0;i<bytes.length;i++){
            System.arraycopy(bytes[i], 0, block, blockOfs, bytes[i].length);
            blockOfs += (bytes[i].length + 1);
        }
        return block;
    }
    
    protected static String c2str(byte[] bytes, int ofs, int length){
        if ( bytes==null )
            return null;
        if ( bytes[ofs]==0 )
            return "";
        return new String(bytes,ofs,length,fieldCharset).trim();
    }
    
    protected static native long mdapiCreateApi(MdApi instance, byte[] flowPath,boolean useUdp,boolean isMulticast);
    
    protected static native void mdapiConnect(MdApi instance, long mdApiPtr, byte[] frontUrl, byte[] brokerId, byte[] userId, byte[] password );
    
    protected static native void mdapiClose(MdApi instance, long mdApiPtr, boolean logout);
    
    protected static native int mdapiSubscribeMarketData(MdApi instance, long mdApiPtr, byte[] instrumentIDs);
    
    protected static native int mdapiUnSubscribeMarketData(MdApi instance, long mdApiPtr, byte[] instrumentIDs);
    
    protected static native long traderapiCreateApi(TraderApi instance, byte[] flowPath);
    
    protected static native void traderapiConnect(TraderApi instance, long traderApiPtr, byte[] frontUrl);
    
    protected static native void traderapiClose(TraderApi instance, long traderApiPtr);
    
    protected static native int traderapiReq(TraderApi instance, long traderApiPtr, int methodId, byte[] field, int nRequestID);
    
    protected static native void traderapiSubscribeTopic(TraderApi instance, long traderApiPtr, boolean publicTopic, int resumeType);
    
    protected static native byte[] traderapiGetTradingDay(TraderApi instance, long traderApiPtr);
}
