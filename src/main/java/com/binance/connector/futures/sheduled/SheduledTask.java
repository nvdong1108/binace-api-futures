package com.binance.connector.futures.sheduled;


import com.binance.connector.futures.client.exceptions.BinanceClientException;
import com.binance.connector.futures.client.exceptions.BinanceConnectorException;
import com.binance.connector.futures.client.impl.UMFuturesClientImpl;
import com.binance.connector.futures.config.PrivateConfig;
import com.binance.connector.futures.controller.ApiOrdersController;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.LinkedHashMap;

@Component
public class SheduledTask {


    private final static Logger logger = LoggerFactory.getLogger(SheduledTask.class);

    private BigDecimal SPACE_PRICE =  new BigDecimal("200");
    
    private final double TRADE_QUANTITY= 0.01;

    private final double MIN_HOLD_QUANTITY= 0.1;

    private double START_PRICE = 71000;

    private Double positionAmt= null;

    @Autowired
    ApiOrdersController apiOrdersContronller;

    @Autowired
    MyStartupRunner myStartupRunner;

    UMFuturesClientImpl client  = new UMFuturesClientImpl(PrivateConfig.TESTNET_API_KEY, PrivateConfig.TESTNET_SECRET_KEY, PrivateConfig.TESTNET_BASE_URL); 
    LinkedHashMap<String, Object> parameters ; 

    //@Scheduled(cron = "*/5 * * * * *")
    private void callOrder(){
        if(myStartupRunner.getResultInitSuccess() && positionAmt!=null){
            //checkConditionCreateNewOrders();
        }
    }
   // @Scheduled(cron = "*/12 * * * * *")
    private void loadInformation() {
        getPositionCurrent();
    }

    

    private synchronized JSONObject getPositionCurrent(){
        LinkedHashMap<String,Object> parameters = new LinkedHashMap<>();
        parameters.put("symbol", "BTCUSDT");
        String result = client.account().positionInformation(parameters);
        if(result==null){
            return null;
        }
        JSONArray jsonArray = new JSONArray();
        JSONObject jsonObject = jsonArray.getJSONObject(0);
        positionAmt =Double.parseDouble(jsonObject.getString("positionAmt"));

        return jsonObject;
    }
    
    private synchronized boolean checkConditionCreateNewOrders(){
        JSONObject tradeSuccess =transactionRecent();
        String mostRecentSide = tradeSuccess.getString("side");
        BigDecimal priceTransferRecentMost = new BigDecimal(tradeSuccess.getString("price"));

        parameters = new LinkedHashMap<>();
        parameters.put("symbol", "BTCUSDT");
        JSONArray allOpenOrders = new JSONArray(client.account().currentAllOpenOrders(parameters));
        
        BigDecimal newOrderBuyMax = null;
        BigDecimal newOrderSELLLowest = null;
        for(int i = 0 ; i < allOpenOrders.length() ; i++){
            JSONObject jsonObject = allOpenOrders.getJSONObject(i);
            BigDecimal priceOfOrder = new BigDecimal(jsonObject.getString("price"));
            if("BUY".equals(jsonObject.getString("side"))){
                if(newOrderBuyMax ==null || newOrderBuyMax.compareTo(priceOfOrder)==-1){
                    newOrderBuyMax=priceOfOrder;
                }
            }else if("SELL".equals(jsonObject.getString("side"))) {
                if(newOrderSELLLowest==null || newOrderSELLLowest.compareTo(priceOfOrder)==1){
                    newOrderSELLLowest = priceOfOrder;
                }
            }
        }

        if("BUY".equals(mostRecentSide)){
            if(newOrderBuyMax == null){
                // not order BUy Next => new Order BUY
                apiOrdersContronller.newOrders(priceTransferRecentMost.subtract(SPACE_PRICE).doubleValue(),TRADE_QUANTITY, "BUY");
            }
            BigDecimal priceOrderSELLCalcu = priceTransferRecentMost.add(SPACE_PRICE);
            if(newOrderSELLLowest ==null){
                double priceOrderSELL =priceOrderSELLCalcu.doubleValue();
                apiOrdersContronller.newOrders(priceOrderSELL,TRADE_QUANTITY, "SELL");
                return true;
            }
            int resultCompare = comparePrice(priceOrderSELLCalcu,newOrderSELLLowest);
            if(resultCompare == 0){
                return false;
            }
            if(resultCompare == 1){
                apiOrdersContronller.newOrders(priceOrderSELLCalcu.doubleValue(),TRADE_QUANTITY, "SELL");
                return true;
            }
            priceOrderSELLCalcu = priceOrderSELLCalcu.add(SPACE_PRICE);
            if(resultCompare == 2){
                apiOrdersContronller.newOrders(priceOrderSELLCalcu.doubleValue(),TRADE_QUANTITY, "SELL");
                return true;
            }
            priceOrderSELLCalcu = priceOrderSELLCalcu.add(SPACE_PRICE);
            if(resultCompare == 3){
                apiOrdersContronller.newOrders(priceOrderSELLCalcu.doubleValue(),TRADE_QUANTITY, "SELL");
                return true;
            }
            priceOrderSELLCalcu = priceOrderSELLCalcu.add(SPACE_PRICE);
            if(resultCompare == 4){
                apiOrdersContronller.newOrders(priceOrderSELLCalcu.doubleValue(),TRADE_QUANTITY, "SELL");
                return true;
            }
            priceOrderSELLCalcu = priceOrderSELLCalcu.add(SPACE_PRICE);
            if(resultCompare == 5){
                apiOrdersContronller.newOrders(priceOrderSELLCalcu.doubleValue(),TRADE_QUANTITY, "SELL");
                return true;
            }
            return false;
        }
        if("SELL".equals(mostRecentSide)){
            if(newOrderSELLLowest==null){
                // not Order SELL Open => create new Order SELL . 
                // check quantity
                //todo
            }
            BigDecimal priceOrderBUYCalcu = priceTransferRecentMost.subtract(SPACE_PRICE);
            if(newOrderBuyMax == null){
                double priceOrderBUY =priceOrderBUYCalcu.doubleValue();
                apiOrdersContronller.newOrders(priceOrderBUY,TRADE_QUANTITY, "BUY");
                return true;
            }
            int resultCompare = comparePrice(priceOrderBUYCalcu,newOrderBuyMax);
            if(resultCompare == 0){
                return false;
            }
            if(resultCompare == 1){
                apiOrdersContronller.newOrders(priceOrderBUYCalcu.doubleValue(),TRADE_QUANTITY, "BUY");
                return true;
            }
            priceOrderBUYCalcu = priceOrderBUYCalcu.subtract(SPACE_PRICE);
            if(resultCompare == 2){
                apiOrdersContronller.newOrders(priceOrderBUYCalcu.doubleValue(),TRADE_QUANTITY, "BUY");
                return true;
            }
            priceOrderBUYCalcu = priceOrderBUYCalcu.subtract(SPACE_PRICE);
            if(resultCompare == 3){
                apiOrdersContronller.newOrders(priceOrderBUYCalcu.doubleValue(),TRADE_QUANTITY, "BUY");
                return true;
            }
            priceOrderBUYCalcu = priceOrderBUYCalcu.subtract(SPACE_PRICE);
            if(resultCompare == 4){
                apiOrdersContronller.newOrders(priceOrderBUYCalcu.doubleValue(),TRADE_QUANTITY, "BUY");
                return true;
            }
            priceOrderBUYCalcu = priceOrderBUYCalcu.subtract(SPACE_PRICE);
            if(resultCompare == 5){
                apiOrdersContronller.newOrders(priceOrderBUYCalcu.doubleValue(),TRADE_QUANTITY, "BUY");
                return true;
            }
        }
        return false;
    }

    private JSONObject dataAllOrderOpen(){

        LinkedHashMap<String, Object> parameters=new LinkedHashMap<>();
        parameters.put("symbol", "BTCUSDT");
        JSONArray allOpenOrders = new JSONArray(client.account().currentAllOpenOrders(parameters));
        
        JSONArray jsonArraySELL = new JSONArray();
        JSONArray jsonArrayBUY = new JSONArray();
        
        for(int i = 0 ; i < allOpenOrders.length() ; i++){
            JSONObject jsonObject = allOpenOrders.getJSONObject(i);
            if("BUY".equals(jsonObject.getString("side"))){
                jsonArrayBUY.put(jsonObject);
            }else if("SELL".equals(jsonObject.getString("side"))) {
                jsonArraySELL.put(jsonObject);
            }
        }
        return null;
    }



    private JSONObject transactionRecent(){
        parameters = new LinkedHashMap<>();
        parameters.put("symbol", "BTCUSDT");
        parameters.put("limit",1);
        String result = client.account().accountTradeList(parameters);
        JSONArray jsonArray = new JSONArray(result);
        return (jsonArray==null|| jsonArray.isEmpty())?null:(jsonArray.getJSONObject(0));
    }

    

   private int comparePrice(BigDecimal price , BigDecimal priceCompare) {
    BigDecimal spacePrice =  price.subtract(priceCompare).abs();
    if(spacePrice.compareTo(new BigDecimal(50)) !=1){
        return 0;
    }
    spacePrice=  spacePrice.subtract(SPACE_PRICE).abs();
    if(spacePrice.compareTo(new BigDecimal(50))!=1){
        return 1;
    }
    spacePrice=  spacePrice.subtract(SPACE_PRICE).abs();
    if(spacePrice.compareTo(new BigDecimal(50))!=1){
        return 2;
    }
    spacePrice=  spacePrice.subtract(SPACE_PRICE).abs();
    if(spacePrice.compareTo(new BigDecimal(50))!=1){
        return 3;
    }
    spacePrice=  spacePrice.subtract(SPACE_PRICE).abs();
    if(spacePrice.compareTo(new BigDecimal(50))!=1){
        return 4;
    }
    spacePrice=  spacePrice.subtract(SPACE_PRICE).abs();
    if(spacePrice.compareTo(new BigDecimal(50))!=1){
        return 5;
    }
    return -999;
   }
   

}
