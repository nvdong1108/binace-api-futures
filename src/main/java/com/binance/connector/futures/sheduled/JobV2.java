package com.binance.connector.futures.sheduled;

import org.springframework.stereotype.Component;

import com.binance.connector.futures.common.Common;
import com.binance.connector.futures.controller.ApiController;
import com.binance.connector.futures.controller.ApiFirebase;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
@Component
public class JobV2 {

    @Autowired
    ApiController apiController;

    @Autowired
    MyStartupRunner myStartupRunner;

    @Autowired
    ApiFirebase firebase;
    
    boolean paramFlagRunOneTime = true ;


    @Scheduled(fixedDelay = 7000)
    private void updateDocument(){
        if(myStartupRunner.getResultInitSuccess()){
            loadOrder();
        }
    }

    private  synchronized void loadOrder(){
        JSONArray jsonArray = apiController.getTradeHistory();
        if(jsonArray == null){
            return;
        }
        for(int i =0; i< jsonArray.length() ; i++){
            JSONObject jsonObject= jsonArray.getJSONObject(i);
            String side = jsonObject.getString("side");
            String orderId=Common.convertObectToString(jsonObject.get("orderId"));
            Map<String,Object> map= firebase.getDoucment(orderId);
            if(map==null){
                continue;
            }
            if(side.equals("BUY")){
                String statusBuy = (String)map.get("status-buy");
                int priceBuy = Common.convertObectToInt(map.get("price-buy"));
                if(statusBuy.equals("NEW")){
                    // create order SELL
                   String result= apiController.newOrders(priceBuy, 0.01, "SELL");
                   JSONObject orderSell = new JSONObject(result);
                   String idSell = Common.convertObectToString(orderSell.get("orderId"));
                   int priceSell = Common.convertObectToInt(orderSell.get("price"));
                    Map<String,Object> document = new HashMap<>();
                    document.put("status-buy", "DONE");
                    document.put("status-sell", "NEW");
                    document.put("id-sell", idSell);
                    document.put("price-sell-open", priceSell);
                    firebase.updateDocumentField(orderId,document);
                }
            }else if(side.equals("SELL")){

            }
        }
    }
    
}
