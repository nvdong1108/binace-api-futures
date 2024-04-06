package com.binance.connector.futures.sheduled;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
        priceBegin=getBeginPrice();
        api.cancelAllOpenOrders();
        sizePositionBegin=api.getSizePosition();
        log.info("\n\n"+
                 "------>   START BOT WITH : Price begin = {} , Positions Size = {}   <-----\n",priceBegin,sizePositionBegin);
        openAllOrder();
    }

    private void openAllOrder(){
        int priceOpenOrder = priceBegin;
        for(int i = 0 ; i < Constant.QUANTIYY_OPEN_ORDES ; i ++){
            priceOpenOrder = priceOpenOrder - Constant.SPACE_PRICE_INT;
            api.newOrders(priceOpenOrder,Constant.QUANTITY_ONE_EXCHANGE, "BUY");
        }
        setResultInitSuccess(true);
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
        priceBegin = firebase.get(fieldName);
        if(priceBegin== Constant.NOT_FOUND){
            priceBegin=api.getMarkPrice();
            firebase.add(fieldName, priceBegin);
        }
        return priceBegin;
    }
    
}
