package com.binance.connector.futures.sheduled;

import java.util.Date;

import org.json.JSONObject;
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
    private boolean createBUYsuccess = false;
    public static int priceBegin;
    public static double sizePositionBegin;
    private final static Logger log = LoggerFactory.getLogger(MyStartupRunner.class);
    

    @Autowired
    ApiController api;

    @Autowired
    ApiFirebase firebase ; 

    @PostConstruct
    public void init() {
        long statusBot = firebase.get("statusBot");
        if(statusBot==-1){
            createNewBot();
        }
        setResultInitSuccess(true);
    }

    public void continuteBot(){

    }
    public synchronized void createNewBot(){
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
            priceOpenOrder = priceOpenOrder - Constant.SPACE_PRICE_INT;
            String result = api.newOrdersFirstTime(priceOpenOrder,Constant.QUANTITY_ONE_EXCHANGE, "BUY");
            if(i== 0){
                JSONObject jsonObject = new JSONObject(result);
                long fromId =Common.convertObectToLong(jsonObject.get("orderId"));
                firebase.add("fromId", fromId);
            }
            firebase.addOrderBuy(result);
        }
    }
    public boolean getResultInitSuccess(){
        return this.initSuccess;
    }
    public void setResultInitSuccess(boolean result){
        this.initSuccess =result;
    }

    public boolean getResultCreateBuySuccess(){
        return this.createBUYsuccess;
    }
    public void setResultCreateBuySuccess(boolean result){
        this.createBUYsuccess =result;
    }

    public boolean getResultRunJobSell(){
        return (this.initSuccess && this.createBUYsuccess);
    }

    private int getBeginPrice(){
        String fieldName = "begin-price";
        priceBegin =Integer.parseInt(firebase.get(fieldName)+"");
        if(priceBegin== Constant.NOT_FOUND){
            priceBegin=(api.getMarkPrice()/100)*100;
            firebase.add(fieldName, priceBegin);
        }
        return priceBegin;
    }
    
}
