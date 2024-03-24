package com.binance.connector.futures.sheduled;


import com.binance.connector.futures.client.exceptions.BinanceClientException;
import com.binance.connector.futures.client.exceptions.BinanceConnectorException;
import com.binance.connector.futures.client.impl.UMFuturesClientImpl;
import com.binance.connector.futures.config.PrivateConfig;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.LinkedHashMap;

@Component
public class SheduledTask {


    private final static Logger logger = LoggerFactory.getLogger(SheduledTask.class);

    private BigDecimal SPACE_PRICE =  new BigDecimal("200");
    
    private double quantity = 0.01;

    UMFuturesClientImpl client ; 
    LinkedHashMap<String, Object> parameters ; 

    @Scheduled(cron = "*/5 * * * * *")
    private void callOrder(){
        logger.info(" ****** Begin job ****** ");
        parameters = new LinkedHashMap<>();
        client = new UMFuturesClientImpl(PrivateConfig.TESTNET_API_KEY, PrivateConfig.TESTNET_SECRET_KEY, PrivateConfig.TESTNET_BASE_URL);
        try {
            //getPriceCurrent();
            checkConditionCreateNewOrders();
        } catch (BinanceConnectorException e) {
            logger.error("fullErrMessage: {}", e.getMessage(), e);
        } catch (BinanceClientException e) {
            logger.error("fullErrMessage: {} \nerrMessage: {} \nerrCode: {} \nHTTPStatusCode: {}",
                    e.getMessage(), e.getErrMsg(), e.getErrorCode(), e.getHttpStatusCode(), e);
        }

    }
    
    private synchronized boolean checkConditionCreateNewOrders(){
        parameters = new LinkedHashMap<>();
        parameters.put("symbol", "BTCUSDT");
        parameters.put("limit",1);
        //case 1 . count orders . 
        // step 1 . check transaction  recent most is ( BUY or SELL ) 
        String result = client.account().accountTradeList(parameters);
        JSONArray jsonArray = new JSONArray(result);
        JSONObject tradeSuccess = jsonArray.getJSONObject(0);
        String mostRecentSide = tradeSuccess.getString("side");

        JSONArray allOpenOrders = new JSONArray(client.account().currentAllOpenOrders(parameters));
        BigDecimal priceTransferRecentMost = new BigDecimal(tradeSuccess.getString("price"));
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
                newOrders(priceTransferRecentMost.subtract(SPACE_PRICE).doubleValue(), "BUY");
            }
            BigDecimal priceOrderSELLCalcu = priceTransferRecentMost.add(SPACE_PRICE);
            if(newOrderSELLLowest ==null){
                double priceOrderSELL =priceOrderSELLCalcu.doubleValue();
                newOrders(priceOrderSELL, "SELL");
                return true;
            }
            int resultCompare = comparePrice(priceOrderSELLCalcu,newOrderSELLLowest);
            if(resultCompare == 0){
                return false;
            }
            if(resultCompare == 500){
                newOrders(priceOrderSELLCalcu.doubleValue(), "SELL");
                return true;
            }
            priceOrderSELLCalcu = priceOrderSELLCalcu.add(SPACE_PRICE);
            if(resultCompare == 1000){
                newOrders(priceOrderSELLCalcu.doubleValue(), "SELL");
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

            BigDecimal priceOrderBUYCalcu = priceTransferRecentMost.add(SPACE_PRICE);
            if(newOrderBuyMax == null){
                double priceOrderBUY =priceOrderBUYCalcu.doubleValue();
                newOrders(priceOrderBUY, "BUY");
            }
            int resultCompare = comparePrice(priceOrderBUYCalcu,newOrderBuyMax);
            if(resultCompare == 0){
                return false;
            }
            if(resultCompare == 500){
                newOrders(priceOrderBUYCalcu.doubleValue(), "BUY");
                return true;
            }
            priceOrderBUYCalcu = priceOrderBUYCalcu.add(SPACE_PRICE);
            if(resultCompare == 1000){
                newOrders(priceOrderBUYCalcu.doubleValue(), "BUY");
                return true;
            }

        }

        return false;
    }

    private void newOrders(double price,String side){
        parameters = new LinkedHashMap<>();
        parameters.put("symbol", "BTCUSDT");
        parameters.put("side", side);
        parameters.put("type", "LIMIT");
        parameters.put("timeInForce", "GTC");
        parameters.put("quantity", quantity);
        parameters.put("price", price);

        try {
            String result = client.account().newOrder(parameters);
            logger.info(result);
        } catch (BinanceConnectorException e) {
            logger.error("fullErrMessage: {}", e.getMessage(), e);
        } catch (BinanceClientException e) {
            logger.error("fullErrMessage: {} \nerrMessage: {} \nerrCode: {} \nHTTPStatusCode: {}",
                    e.getMessage(), e.getErrMsg(), e.getErrorCode(), e.getHttpStatusCode(), e);
        }
    }

   private int comparePrice(BigDecimal price , BigDecimal priceCompare) {
    BigDecimal spacePrice =  price.subtract(priceCompare).abs();
    if(spacePrice.compareTo(new BigDecimal(50)) !=1){
        return 0;
    }
    spacePrice=  spacePrice.subtract(SPACE_PRICE).abs();
    if(spacePrice.compareTo(new BigDecimal(50))!=1){
        return 500;
    }
    spacePrice=  spacePrice.subtract(SPACE_PRICE).abs();
    if(spacePrice.compareTo(new BigDecimal(50))!=1){
        return 1000;
    }
    return -999;
   }
   

}
