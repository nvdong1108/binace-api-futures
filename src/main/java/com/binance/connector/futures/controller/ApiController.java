package com.binance.connector.futures.controller;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.binance.connector.futures.client.exceptions.BinanceClientException;
import com.binance.connector.futures.client.exceptions.BinanceConnectorException;
import com.binance.connector.futures.client.impl.UMFuturesClientImpl;
import com.binance.connector.futures.config.Constant;
import com.binance.connector.futures.config.PrivateConfig;

@Component
public class ApiController {

    private final static Logger logger = LoggerFactory.getLogger(ApiController.class);
    UMFuturesClientImpl client  = new UMFuturesClientImpl(PrivateConfig.TESTNET_API_KEY, PrivateConfig.TESTNET_SECRET_KEY, PrivateConfig.TESTNET_BASE_URL); 

    public void newOrders(double price, double quantity, String side){
        try {
            DecimalFormat decimalFormat = new DecimalFormat("#.##");
            LinkedHashMap<String, Object> parameters  = new LinkedHashMap<>();
            logger.info(" BEGIN : Create New Order {} price {}",side,price);
            
            parameters = new LinkedHashMap<>();
            parameters.put("symbol", "BTCUSDT");
            parameters.put("side", side);
            parameters.put("type", "LIMIT");
            parameters.put("timeInForce", "GTC");
            parameters.put("quantity", decimalFormat.format(quantity));
            parameters.put("price", Math.floor(price));
       
            String result = client.account().newOrder(parameters);
            logger.info(" RETURN : create Order {} ",result);
        } catch (BinanceConnectorException e) {
            logger.error("fullErrMessage: {}", e.getMessage(), e);
        } catch (BinanceClientException e) {
            logger.error("fullErrMessage: {} \nerrMessage: {} \nerrCode: {} \nHTTPStatusCode: {}",
                    e.getMessage(), e.getErrMsg(), e.getErrorCode(), e.getHttpStatusCode(), e);
        }
    }

     public Double getMarkPrice(){
        LinkedHashMap<String, Object> parameters = new LinkedHashMap<>();
        parameters.put("symbol", Constant.SYMBOL);
        String result = client.market().markPrice(parameters);
        JSONObject jsonObject = new JSONObject(result);
        String markPrice = jsonObject.getString("markPrice");
        logger.info("\n get price market BTCUSDT={}",markPrice,"\n");
        return  Double.valueOf((int)Double.parseDouble(markPrice));
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

    public  JSONArray getCurrentAllOpenOrders(){
        LinkedHashMap<String, Object> parameters=new LinkedHashMap<>();
        parameters.put("symbol", Constant.SYMBOL);
        JSONArray allOpenOrders = new JSONArray(client.account().currentAllOpenOrders(parameters));
        return allOpenOrders;
    }

    public List<Double> getListpricesOpens(String side){
        JSONArray  jsonArray = getCurrentAllOpenOrders();
        if(jsonArray==null || jsonArray.isEmpty()){
            return null;
        }
        List<Double> listPrice = new ArrayList<>();
        for(int i = 0 ; i < jsonArray.length() ; i ++){
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            String sideOpens = jsonObject.getString("side");
            String price = jsonObject.getString("price");
            if(side.equals(sideOpens) ){
                Double result = Double.valueOf((int)Double.parseDouble(price));
                listPrice.add(result);
            }
        }
        if(Constant.SIDE_BUY.equals(side)){
            Collections.sort(listPrice,new Comparator<Double>() {
                @Override
                public int compare(Double d1 , Double d2){
                    return d2.compareTo(d1);
                }
            });
        }else {
            Collections.sort(listPrice,new Comparator<Double>() {
                @Override
                public int compare(Double d1 , Double d2){
                    return d1.compareTo(d2);
                }
            });
        }
        
        return listPrice;
    }


    public void cancelAllOpenOrders(){
        LinkedHashMap<String, Object> parameters=new LinkedHashMap<>();
        parameters.put("symbol", Constant.SYMBOL);
        String result = client.account().cancelAllOpenOrders(parameters);

    }


    



}
