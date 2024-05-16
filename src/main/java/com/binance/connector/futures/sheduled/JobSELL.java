package com.binance.connector.futures.sheduled;

import com.binance.connector.futures.config.PrivateConfig;
import com.binance.connector.futures.controller.BotPutMessageLog;
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
public class JobSELL {


    @Autowired
    ApiController apiController;

    @Autowired
    MyStartupRunner myStartupRunner;

    @Autowired
    ApiFirebase firebase;

    @Autowired
    BotPutMessageLog botPutMessageLog;

    @Scheduled(fixedDelay = 6500)
    private void updateDocument() {
        if (myStartupRunner.isRunBotSell()) {
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
                if (PrivateConfig.isContentOrderIdIgnoreSell(orderId)) {
                    continue;
                }
                Map<String, Object> map = firebase.getDoucment("SELL_" + orderId);
                if (map == null) {
                    PrivateConfig.addOrderIdIgnoreSell(orderId);
                    continue;
                }
                if (side.equalsIgnoreCase("SELL")) {
                    String status = (String) map.get("status-sell");
                    if (status.equals("NEW")) {
                        // create order BUY NEW
                        int priceSell = Common.convertObectToInt(map.get("price-sell"));
                        int priceOpenBuy = priceSell - MyStartupRunner.getSpacePriceBenefit();
                        String result = apiController.newOrders(priceOpenBuy, Constant.QUANTITY_ONE_EXCHANGE, "BUY");
                        if (result == null || result.isBlank()) {
                            continue;
                        }
                        JSONObject orderSell = new JSONObject(result);

                        String idBuy = Common.convertObectToString(orderSell.get("orderId"));
                        int priceBuyMaker = Common.convertObectToInt(orderSell.get("price"));
                        long time = Common.convertObectToLong(orderSell.get("updateTime"));
                        Date date = new Date(time);
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        String dateFormat = simpleDateFormat.format(date);
                        map.put("status-sell", "DONE");
                        map.put("status-buy", "NEW");
                        map.put("id-buy", idBuy);
                        map.put("price-buy-open", priceBuyMaker);
                        map.put("time-buy", dateFormat);
                        firebase.addOrder("SELL_" + idBuy, map);
                        firebase.delete("SELL_" + orderId, Constant.FB_POSITIONS);
                    }
                } else if (side.equals("BUY")) {
                    String statusBuy = (String) map.get("status-buy");
                    if ("NEW".equals(statusBuy)) {
                        // step 1 . new buy
                        int priceSelOld = Common.convertObectToInt(map.get("price-sell"));
                        String result = apiController.newOrders(priceSelOld, Constant.QUANTITY_ONE_EXCHANGE, "SELL");
                        if (result == null || result.isBlank()) {
                            continue;
                        }
                        firebase.addOrder(result, "SELL");
                        // step 2 . update firebase.
                        JSONObject jsonOb = new JSONObject(result);
                        String idSellNew = Common.convertObectToString(jsonOb.get("orderId"));
                        long priceBuySuccess = Common.convertObectToLong(jsonObject.get("price"));
                        String qtyBuy = Common.convertObectToString(jsonObject.get("qty"));
                        map.put("id-sell-next", idSellNew);
                        map.put("status-buy", "DONE");
                        map.put("price-buy-success", priceBuySuccess);
                        map.put("qty-buy", qtyBuy);
                        // firebase.updateDocumentField(orderId, map);
                        firebase.delete("SELL_" + orderId, Constant.FB_POSITIONS);
                        firebase.addOrderLog(orderId, map, "SELL");
                        // check
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            botPutMessageLog.post(ex.getMessage());
            PrivateConfig.resetOrderIdIgnoreSell();
        }
    }

}
