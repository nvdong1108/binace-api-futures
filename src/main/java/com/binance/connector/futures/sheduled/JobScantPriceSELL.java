package com.binance.connector.futures.sheduled;

import java.util.LinkedHashMap;

import com.binance.connector.futures.client.impl.UMWebsocketClientImpl;
import com.binance.connector.futures.controller.BotPutMessageLog;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.binance.connector.futures.common.Common;
import com.binance.connector.futures.config.Constant;
import com.binance.connector.futures.controller.ApiController;
import com.binance.connector.futures.controller.ApiFirebase;

import javax.annotation.PostConstruct;

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

    private synchronized void openOrderNew(long price){
        try {
            if(priceSellNext==null){
                return;
            }
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

    @PostConstruct
    private  void loadPriceWebSocket() {
        System.out.println("start ");
        UMWebsocketClientImpl client = new UMWebsocketClientImpl();
        client.markPriceStream("btcusdt",1 ,((event)->{
            JSONObject jsonObject = new JSONObject(event);
            long price = Common.convertObectToLong(jsonObject.get("p"));
            System.out.println("Price BTC : "+price);
            openOrderNew(price);
        }));
    }
}
