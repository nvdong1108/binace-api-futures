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
    public static int priceBegin;
    public static double sizePositionBegin;
    private final static Logger log = LoggerFactory.getLogger(MyStartupRunner.class);

    private static long startTime = -1l;
    private static int spacePriceInt = 0;

    private static String botType = null;
    

    @Autowired
    ApiController api;

    @Autowired
    ApiFirebase firebase ; 

    @PostConstruct
    public void init() {
        try{
            long statusBot = Common.convertObectToLong(firebase.get("statusBot"));
            botType = (String)firebase.get("bot-type");
            if(botType == null ){
                throw new Exception("don't get bot type value");
            }
            int _spacePriceInt =Common.convertObectToInt(firebase.get("spacePriceInt"));
            if(_spacePriceInt == -1){
                throw new Exception("don't get space price value");
            }
            setSpacePriceInt(_spacePriceInt);
    
            if(statusBot==-1){
                long startTime = new Date().getTime();
                firebase.add("startTime", startTime);
                setStartTime(startTime);
                createNewBot();
            }else {
                long _startTime = (long)firebase.get("startTime");
                setStartTime(_startTime);
            }
            setResultInitSuccess(true);
        }catch (Exception ex){
            log.error("\n\t\t****RETURN ERROR : {}\n",ex.getMessage());
        }
    }


    public synchronized void createNewBot() throws Exception{
        priceBegin=getBeginPrice();
        api.cancelAllOpenOrders();
        firebase.deleAll("LogSuccessOrder");
        firebase.deleAll("positions");
        sizePositionBegin=api.getSizePosition();
        log.info("\n\n"+
                 "------>   START BOT WITH : Price begin = {} , Positions Size = {}   <-----\n",priceBegin,sizePositionBegin);
        openAllOrder();
        firebase.add("statusBot",1l);
    }

    private void openAllOrder(){
        int priceOpenOrder = priceBegin;
        for(int i = 0 ; i < Constant.QUANTIYY_OPEN_ORDES ; i ++){
            if(i>0) {
                if(isBotBuy()){
                    priceOpenOrder = priceOpenOrder - getSpacePriceInt();
                }else if(isBotSell()){
                    priceOpenOrder = priceOpenOrder + getSpacePriceInt();
                }
            }
            String  result = api.newOrdersFirstTime(priceOpenOrder,Constant.QUANTITY_ONE_EXCHANGE, botType);
            firebase.addOrder(result);
        }
    }
    
    private int getBeginPrice() throws Exception{
        String fieldName = "begin-price";
        priceBegin =Common.convertObectToInt(firebase.get(fieldName)+"");

        if(priceBegin== Constant.NOT_FOUND){
           throw new Exception("\n\t\tERROR don't get begin price value");
        }
        return priceBegin;
    }
    public void setResultInitSuccess(boolean result){
        this.initSuccess =result;
    }

    public static long getStartTime() {
        return startTime;
    }

    public static void setStartTime(long startTime) {
        MyStartupRunner.startTime = startTime;
    }

    public static int getSpacePriceInt() {
        return spacePriceInt;
    }
    public static int getSpacePriceBenefit() {
        return (spacePriceInt*3)/2;
    }

    public static void setSpacePriceInt(int spacePriceInt) {
        MyStartupRunner.spacePriceInt = spacePriceInt;
    }
    private boolean isBotBuy(){
        return Constant.SIDE_BUY.equalsIgnoreCase(MyStartupRunner.botType);
    }
    private boolean isBotSell(){
        return Constant.SIDE_SELL.equalsIgnoreCase(MyStartupRunner.botType);
    }

    public boolean isRunBotBuy() {
        return this.initSuccess && Constant.SIDE_BUY.equalsIgnoreCase(MyStartupRunner.botType);
    }

    public boolean isRunBotSell() {
        return this.initSuccess && Constant.SIDE_SELL.equalsIgnoreCase(MyStartupRunner.botType);
    }
}
