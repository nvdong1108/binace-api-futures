package com.binance.connector.futures.sheduled;

import org.springframework.stereotype.Component;

import com.binance.connector.futures.common.Common;
import com.binance.connector.futures.config.Constant;
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
        if(jsonArray == null || jsonArray.isEmpty()){
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
                if(statusBuy.equals("NEW")){
                    // create order SELLNEW
                    int priceBuy = Common.convertObectToInt(map.get("price-buy"));
                    int priceOpenSell = priceBuy+Constant.SPACE_PRICE_INT;
                     String result= apiController.newOrders(priceOpenSell, 0.01, "SELL");
                   JSONObject orderSell = new JSONObject(result);

                   String idSell = Common.convertObectToString(orderSell.get("orderId"));
                   int priceSell = Common.convertObectToInt(orderSell.get("price"));
                    
                    map.put("status-buy", "DONE");
                    map.put("status-sell", "NEW");
                    map.put("id-sell", idSell);
                    map.put("price-sell-open", priceSell);
                    firebase.addOrder(idSell, map);
                    firebase.delete(orderId);
                }
            }else if(side.equals("SELL")){
                String statusSell = (String)map.get("status-sell");
                if("NEW".equals(statusSell)){
                    // step 1 . new buy 
                    int priceBuyOld =Common.convertObectToInt(map.get("price-buy"));
                    String result = apiController.newOrders(priceBuyOld, 0.01  , "BUY");
                    firebase.addOrderBuy(result);
                    // step 2 . update firebase. 
                    JSONObject jsonOb = new JSONObject(result);
                    String idBuyNew =Common.convertObectToString(jsonOb.get("orderId"));
                    long priceSellSuccess = Common.convertObectToLong(map.get("price"));
                    map.put("id-buy-next", idBuyNew);
                    map.put("status-sell", "DONE");
                    map.put("price-sell-success", priceSellSuccess);
                    firebase.updateDocumentField(orderId, map);
                    //firebase.delete(orderId);
                    firebase.addOrderLog(orderId, map);
                }
            }
        }
    }
    
}
