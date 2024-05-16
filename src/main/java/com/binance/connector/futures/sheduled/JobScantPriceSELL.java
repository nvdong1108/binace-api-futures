package com.binance.connector.futures.sheduled;

import java.util.LinkedHashMap;

import com.binance.connector.futures.controller.BotPutMessageLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.binance.connector.futures.common.Common;
import com.binance.connector.futures.config.Constant;
import com.binance.connector.futures.controller.ApiController;
import com.binance.connector.futures.controller.ApiFirebase;

@Component
public class JobScantPriceSELL {

    

    @Autowired
    MyStartupRunner myStartupRunner;

     @Autowired
    ApiController apiController;

    @Autowired
    ApiFirebase firebase;

    @Autowired
    BotPutMessageLog botPutMessageLog;

    private static Long priceSellNext = null;

    boolean isUpdatePriceOnFireBase = true;

   @Scheduled(fixedDelay = 7000)
    private void before(){
        if((myStartupRunner.isRunBotSell())){
            try {
                if(priceSellNext==null || isUpdatePriceOnFireBase){
                    getPriceSellOpenLowest();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public synchronized void getPriceSellOpenLowest() throws Exception{
        long price = firebase.getPriceSellLowest();
        if(price>0){
            priceSellNext = price-MyStartupRunner.getSpacePriceInt();
            isUpdatePriceOnFireBase =false;
        }
    }

    @Scheduled(fixedDelay = 5000)
    private synchronized void getPriceCurrent(){
        try {
            if(priceSellNext==null){
                return;
            }
            long price =Common.convertObectToLong(apiController.getPriceCurrent(Constant.SYMBOL));
            if(price<=priceSellNext){
                String result= apiController.newOrders(Integer.parseInt(priceSellNext+""), Constant.QUANTITY_ONE_EXCHANGE, "SELL");
                firebase.addOrderStatusNew(result,"SELL");
                priceSellNext=priceSellNext-MyStartupRunner.getSpacePriceInt();
                isUpdatePriceOnFireBase=true;
            }
        }catch (Exception ex){
            ex.printStackTrace();
            botPutMessageLog.post( String.format(" Error in class JobScantPriceSELL : %s",ex.getMessage()));
        }
    }
}
