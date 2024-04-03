package com.binance.connector.futures.sheduled;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.binance.connector.futures.config.Constant;
import com.binance.connector.futures.controller.ApiController;

import jakarta.annotation.PostConstruct;

@Component
public class MyStartupRunner {

    private boolean initSuccess = false;
    public static int priceBegin;
    public static double sizePositionBegin;
    @Autowired
    ApiController api;

    @PostConstruct
    public void init() {
        priceBegin=getBeginPrice();
        api.cancelAllOpenOrders();
        sizePositionBegin=api.getSizePosition();
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

    private int getBeginPrice(){
        // priceBegin=api.getMarkPrice();
        priceBegin = 68000;
        return priceBegin;
    }
    
}
