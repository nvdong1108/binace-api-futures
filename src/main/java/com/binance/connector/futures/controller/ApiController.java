package com.binance.connector.futures.controller;

import java.text.DecimalFormat;
import java.util.Date;
import java.util.LinkedHashMap;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.binance.connector.futures.client.exceptions.BinanceClientException;
import com.binance.connector.futures.client.exceptions.BinanceConnectorException;
import com.binance.connector.futures.client.impl.UMFuturesClientImpl;
import com.binance.connector.futures.common.Common;
import com.binance.connector.futures.config.Constant;
import com.binance.connector.futures.config.PrivateConfig;

@Component
public class ApiController {

    private final static Logger logger = LoggerFactory.getLogger(ApiController.class);
    UMFuturesClientImpl client  = new UMFuturesClientImpl(PrivateConfig.TESTNET_API_KEY, PrivateConfig.TESTNET_SECRET_KEY, PrivateConfig.TESTNET_BASE_URL); 

    @Autowired
    ApiFirebase firebase ; 
    
    public String newOrders(int price, double quantity, String side){
        try {
            if(!validOpensOrders(price,side)){
                logger.error("\n\n------>    ERROR  Create {} price {} \n", side,price);
                return null;
            }
            DecimalFormat decimalFormat = new DecimalFormat("#.##");
            LinkedHashMap<String, Object> parameters  = new LinkedHashMap<>();
            // if (logger.isDebugEnabled()) {
            //     logger.debug("\n\n------>   BEGIN  : Create New Order {}            <------\n",side);
            // }
            parameters = new LinkedHashMap<>();
            parameters.put("symbol", "BTCUSDT");
            parameters.put("side", side);
            parameters.put("type", "LIMIT");
            parameters.put("timeInForce", "GTC");
            parameters.put("quantity", decimalFormat.format(quantity));
            parameters.put("price", price);
       
           String result =client.account().newOrder(parameters);
            logger.info("\n\n"+
                        "------>   RETURN : creat {}  price={} success  <------\n",side,price);
                        return result;
        } catch (Exception e) {
            logger.error("newOrders Error fullErrMessage: {}", e.getMessage(), e);
            return null;
        }
    }
    public String newOrdersFirstTime(int price, double quantity, String side){
        String result = null;
        try {
            DecimalFormat decimalFormat = new DecimalFormat("#.###");
            LinkedHashMap<String, Object> parameters  = new LinkedHashMap<>();
            logger.info("\n\n------>   BEGIN  : Create New Order {}            <------\n",side);
            
            parameters = new LinkedHashMap<>();
            parameters.put("symbol", "BTCUSDT");
            parameters.put("side", side);
            parameters.put("type", "LIMIT");
            parameters.put("timeInForce", "GTC");
            parameters.put("quantity", decimalFormat.format(quantity));
            parameters.put("price", price);
       
            result=client.account().newOrder(parameters);
            logger.info("\n\n"+
                        "------>   RETURN : creat {}  price={} success  <------\n",side,price);
        } catch (BinanceConnectorException e) {
            logger.error("newOrders Error fullErrMessage: {}", e.getMessage(), e);
        } catch (BinanceClientException e) {
            logger.error("newOrders Error  fullErrMessage: {} \nerrMessage: {} \nerrCode: {} \nHTTPStatusCode: {}",
                    e.getMessage(), e.getErrMsg(), e.getErrorCode(), e.getHttpStatusCode(), e);
        }
        return result;
    }
    private boolean validOpensOrders(int price, String side){
        JSONObject jsonObject = getOneTradeList();
        if(jsonObject==null){
            return true;
        }
        String _sideTrade = jsonObject.getString("side");
        String _priceTrade = jsonObject.getString("price");
        if(!_sideTrade.equalsIgnoreCase(side) ){
            return true; 
        }
        int intPriceTrade = (int)Double.parseDouble(_priceTrade);
        int resultCompare =Common.comparePrice(intPriceTrade, price);
        if(resultCompare!=0){
               return true;
        }
        if(Constant.SIDE_BUY.equals(side)){
            JSONArray jsonArrayBuy=getCurrentAllOpenOrders(side);
            if(jsonArrayBuy==null || jsonArrayBuy.isEmpty()){
                return true;
            }
        }
        return false;
    }
    public JSONArray getTradeHistory(){
        try {
            long fromId = firebase.get("fromId");
            long endTime = new Date().getTime();
            LinkedHashMap<String, Object> parameters  = new LinkedHashMap<>();
            parameters.put("symbol", "BTCUSDT");
            //parameters.put("startTime", startTime);
            // parameters.put("endTime", endTime);
            //parameters.put("fromId", fromId);
            parameters.put("limit", "10");

            String result = client.account().accountTradeList(parameters);
            if(result==null || result.isBlank()){
                return null;
            }
            return  new JSONArray(result);
        }catch(Exception e){
            logger.error(e.getMessage());
            return null;
        }
    }


    public JSONObject getOneTradeList(){
        try {
            LinkedHashMap<String, Object> parameters  = new LinkedHashMap<>();
            parameters.put("symbol", "BTCUSDT");
            parameters.put("limit", "1");
            String result = client.account().accountTradeList(parameters);
            if(result==null || result.isBlank()){
                return null;
            }
            JSONArray jsonArray = new JSONArray(result);
            if(jsonArray.isEmpty()){
                return null;
            }
            return jsonArray.getJSONObject(0);
        }catch(Exception e){
            logger.error(e.getMessage());
            return null;
        }
    }

     public int getMarkPrice(){
        LinkedHashMap<String, Object> parameters = new LinkedHashMap<>();
        parameters.put("symbol", Constant.SYMBOL);
        String result = client.market().tickerSymbol(parameters); 
        JSONObject jsonObject = new JSONObject(result);
        String markPrice = jsonObject.getString("price");
        return  (int)Double.parseDouble(markPrice);
    }

    public Double getSizePosition(){
        LinkedHashMap<String,Object> parameters = new LinkedHashMap<>();
        parameters.put("symbol", Constant.SYMBOL);
        String result = client.account().positionInformation(parameters);
        if(result==null){
            return Double.valueOf(0);
        }
        JSONArray jsonArray = new JSONArray(result);
        JSONObject jsonObject = jsonArray.getJSONObject(0);
        DecimalFormat df = new DecimalFormat("#.####");
        return Double.parseDouble(df.format(Double.parseDouble(jsonObject.getString("positionAmt"))));
    }

    public JSONArray getCurrentAllOpenOrders(String _side){
        LinkedHashMap<String, Object> parameters=new LinkedHashMap<>();
        parameters.put("symbol", Constant.SYMBOL);
        JSONArray allOpenOrders = new JSONArray(client.account().currentAllOpenOrders(parameters));
        if(allOpenOrders==null || allOpenOrders.isEmpty()){
            return null;
        }
        JSONArray result = new JSONArray();
        for(int i = 0 ; i<allOpenOrders.length() ;i++){
            JSONObject jsonObject = allOpenOrders.getJSONObject(i);
            String side = jsonObject.getString("side");
            if(_side.equalsIgnoreCase(side)){
                result.put(jsonObject);
            }
        }
        return result;
    }
    public void cancelAllOpenOrders(){
       
        LinkedHashMap<String, Object> parameters=new LinkedHashMap<>();
        parameters.put("symbol", Constant.SYMBOL);
        client.account().cancelAllOpenOrders(parameters);
    }
}
