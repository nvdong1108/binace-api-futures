package com.binance.connector.futures.sheduled;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.binance.connector.futures.common.Common;
import com.binance.connector.futures.config.Constant;
import com.binance.connector.futures.controller.ApiController;
import com.binance.connector.futures.controller.ApiFirebase;

import jakarta.annotation.PostConstruct;

@Component
public class MyStartupRunner {

    private boolean initSuccess = false;
    //public static int priceBegin;
    public static double sizePositionBegin;
    private final static Logger log = LoggerFactory.getLogger(MyStartupRunner.class);

    public static  long startTime_SEL = -1l;
    public static  long startTime_BUY= -1l;
    private static int spacePriceInt = 0;

    @Autowired
    ApiController api;

    @Autowired
    ApiFirebase firebase ; 

    @PostConstruct
    public void init() {
        try{
            
            int _spacePriceInt =Common.convertObectToInt(firebase.get("spacePriceInt"));
            if(_spacePriceInt == -1){
                throw new Exception("don't get space price value");
            }
            setSpacePriceInt(_spacePriceInt);
    
            long statusBotSELL = Common.convertObectToLong(firebase.get("statusBot_SELL"));
            if(statusBotSELL!=1){
                long startTime = new Date().getTime();
                firebase.add("startTime_SELL", startTime);
                setStartTimeSELL(startTime);
                createNewBot("SELL");
            }else {
                long _startTimeSELL = (long)firebase.get("startTime_SELL");
                setStartTimeSELL(_startTimeSELL);
            }
            // 
            long statusBotBUY = Common.convertObectToLong(firebase.get("statusBot_BUY"));
            if(statusBotBUY!=1){
                long startTime = new Date().getTime();
                firebase.add("startTime_BUY", startTime);
                setStartTimeBUY(startTime);
                createNewBot("BUY");
            }else {
                long _startTimeBUY = (long)firebase.get("startTime_BUY");
                setStartTimeBUY(_startTimeBUY);
            }

            setResultInitSuccess(true);
        }catch (Exception ex){
            log.error("\n\t\t****RETURN ERROR : {}\n",ex);
        }
    }


    public synchronized void createNewBot(String side) throws Exception{
        api.cancelAllOpenOrders(side);
        firebase.deleAll(Constant.SYMBOL+"_log",side);
        firebase.deleAll(Constant.SYMBOL+"_positions",side);
        int priceBeginSide=getBeginPrice(side);
        log.info("\n\n"+
                 "------>   START BOT WITH : Price begin = {}    <-----\n",priceBeginSide);

        openAllOrder(priceBeginSide,side);
        firebase.add("statusBot_"+side,1l);
    }

    private void openAllOrder(int priceOpenOrder, String side ){
        for(int i = 0 ; i < Constant.QUANTIYY_OPEN_ORDES ; i ++){
            if(i>0) {
                if("BUY".equals(side)){
                    priceOpenOrder = priceOpenOrder - getSpacePriceInt();
                }else if("SELL".equals(side)){
                    priceOpenOrder = priceOpenOrder + getSpacePriceInt();
                }
            }
            String  result = api.newOrdersFirstTime(priceOpenOrder,Constant.QUANTITY_ONE_EXCHANGE, side);
            firebase.addOrder(result,side);
        }
    }
    
    private int getBeginPrice(String side) throws Exception{
        String fieldName = "begin-price-"+side;
        int priceBegin =Common.convertObectToInt(firebase.get(fieldName)+"");
        if(priceBegin== Constant.NOT_FOUND){
           throw new Exception("\n\t\tERROR don't get begin price value");
        }
        return priceBegin;
    }
    public void setResultInitSuccess(boolean result){
        this.initSuccess =result;
    }

    public static long getStartTime(String side) {
        if("SELL".equals(side)){
            return  MyStartupRunner.startTime_SEL;
        }else if("BUY".equals(side)){
            return MyStartupRunner.startTime_BUY;
        }
        return -1;
       
    }

    public void setStartTimeSELL(long startTime) {
        this.startTime_SEL = startTime;;
    }
    public void setStartTimeBUY(long startTime) {
        this.startTime_BUY = startTime;;
    }

    public static int getSpacePriceInt() {
        return spacePriceInt;
    }
    public static int getSpacePriceBenefit() {
        return spacePriceInt;
    }

    public static void setSpacePriceInt(int spacePriceInt) {
        MyStartupRunner.spacePriceInt = spacePriceInt;
    }
    public boolean isRunBotBuy() {
        return this.initSuccess ;
    }

    public boolean isRunBotSell() {
        return this.initSuccess ;
    }
}
