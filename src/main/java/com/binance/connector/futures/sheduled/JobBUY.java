package com.binance.connector.futures.sheduled;

import com.binance.connector.futures.config.PrivateConfig;
import com.binance.connector.futures.controller.BotPutMessageLog;
import com.binance.connector.futures.module.NotificationCreateOrderSuccessModule;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import com.binance.connector.futures.common.Common;
import com.binance.connector.futures.config.Constant;
import com.binance.connector.futures.controller.ApiController;
import com.binance.connector.futures.controller.ApiFirebase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;

@Component
public class JobBUY {

    @Autowired
    ApiController apiController;

    @Autowired
    MyStartupRunner myStartupRunner;

    @Autowired
    ApiFirebase firebase;


    @Autowired
    BotPutMessageLog botPutMessageLog;





    @Scheduled(fixedDelay = 7000)
    private void updateDocument() {
        if (myStartupRunner.isRunBotBuy()) {
            loadOrder();
        }
    }

    private synchronized void loadOrder() {
        try {
            JSONArray jsonArray = apiController.getTradeHistory();
            if (jsonArray == null || jsonArray.isEmpty()) {
                return;
            }
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String side = jsonObject.getString("side");
                String orderId = Common.convertObectToString(jsonObject.get("orderId"));
                if (PrivateConfig.isContentOrderIdIgnoreBuy(orderId)) {
                    continue;
                }
                Map<String, Object> map = firebase.getDoucment("BUY_" + orderId);
                if (map == null) {
                    PrivateConfig.addOrderIdIgnoreBuy(orderId);
                    continue;
                }

                if (side.equals("BUY")) {
                    String statusBuy = (String) map.get("status-buy");
                    if (statusBuy.equals("NEW")) {
                        // create order SELLNEW
                        int priceBuy = Common.convertObectToInt(map.get("price-buy"));
                        int priceOpenSell = priceBuy + MyStartupRunner.getSpacePriceBenefit();
                        String result = apiController.newOrders(priceOpenSell, Constant.QUANTITY_ONE_EXCHANGE, "SELL");
                        if (result == null || result.isBlank()) {
                            continue;
                        }

                        JSONObject orderSell = new JSONObject(result);

                        String idSell = Common.convertObectToString(orderSell.get("orderId"));
                        int priceSell = Common.convertObectToInt(orderSell.get("price"));
                        long time = Common.convertObectToLong(orderSell.get("updateTime"));
                        Date date = new Date(time);
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        String dateFormat = simpleDateFormat.format(date);
                        map.put("status-buy", "DONE");
                        map.put("status-sell", "NEW");
                        map.put("id-sell", idSell);
                        map.put("price-sell-open", priceSell);
                        map.put("time-sell", dateFormat);
                        firebase.addOrder(("BUY_" + idSell), map);
                        firebase.delete("BUY_" + orderId, Constant.FB_POSITIONS);
                        // push notification
                        NotificationCreateOrderSuccessModule noti = new NotificationCreateOrderSuccessModule(map,
                                String.format("%s SUCCESS WIDTH INFO",side));
                        botPutMessageLog.post(noti.toString());
                    }
                } else if (side.equals("SELL")) {
                    String statusSell = (String) map.get("status-sell");
                    if ("NEW".equals(statusSell)) {
                        // step 1 . new buy
                        int priceBuyOld = Common.convertObectToInt(map.get("price-buy"));
                        String result = apiController.newOrders(priceBuyOld, Constant.QUANTITY_ONE_EXCHANGE, "BUY");
                        if (result == null || result.isBlank()) {
                            continue;
                        }
                        firebase.addOrderStatusNew(result, "BUY");
                        // step 2 . update firebase.
                        JSONObject jsonOb = new JSONObject(result);
                        String idBuyNew = Common.convertObectToString(jsonOb.get("orderId"));
                        long priceSellSuccess = Common.convertObectToLong(jsonObject.get("price"));
                        String qtySell = Common.convertObectToString(jsonObject.get("qty"));
                        map.put("id-buy-next", idBuyNew);
                        map.put("status-sell", "DONE");
                        map.put("price-sell-success", priceSellSuccess);
                        map.put("qty-sell", qtySell);
                        firebase.delete("BUY_" + orderId, Constant.FB_POSITIONS);
                        firebase.addOrderLog(orderId, map, "BUY");
                        // push notification
                        NotificationCreateOrderSuccessModule noti = new NotificationCreateOrderSuccessModule(map,
                                String.format("%s SUCCESS WIDTH INFO",side));
                        botPutMessageLog.post(noti.toString());
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            botPutMessageLog.post("Error in class JobBUY : " +  ex.getMessage());
            PrivateConfig.resetOrderIdIgnoreBuy();
        }
    }

}
