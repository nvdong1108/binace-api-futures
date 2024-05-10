package com.binance.connector.futures.sheduled;

import java.util.LinkedHashMap;

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
            this.priceSellNext = price-MyStartupRunner.getSpacePriceInt();
            isUpdatePriceOnFireBase =false;
        }
    }

    @Scheduled(fixedDelay = 5000)
    private synchronized void getPriceCurrent(){
        if(priceSellNext==null){
            return;
        }
        long price =Common.convertObectToLong(apiController.getPriceCurrent(Constant.SYMBOL));
        if(price<=priceSellNext){
            String result= apiController.newOrders(Integer.parseInt(priceSellNext+""), Constant.QUANTITY_ONE_EXCHANGE, "SELL");
            firebase.addOrder(result,"SELL");
            priceSellNext=priceSellNext-MyStartupRunner.getSpacePriceInt();
            isUpdatePriceOnFireBase=true;
        }
    }
}
