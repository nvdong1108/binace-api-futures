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
import org.springframework.util.StringUtils;

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

    public void newOrders(int price, double quantity, String side){
        try {
            if(!validOpensOrders(price,side)){
                return;
            }
            DecimalFormat decimalFormat = new DecimalFormat("#.##");
            LinkedHashMap<String, Object> parameters  = new LinkedHashMap<>();
            logger.info(" BEGIN : Create New Order {} price {}",side,price);
            
            parameters = new LinkedHashMap<>();
            parameters.put("symbol", "BTCUSDT");
            parameters.put("side", side);
            parameters.put("type", "LIMIT");
            parameters.put("timeInForce", "GTC");
            parameters.put("quantity", decimalFormat.format(quantity));
            parameters.put("price", price);
       
            String result = client.account().newOrder(parameters);
            logger.info(" RETURN : create Order {}  {} ",side,result);
        } catch (BinanceConnectorException e) {
            logger.error("fullErrMessage: {}", e.getMessage(), e);
        } catch (BinanceClientException e) {
            logger.error("fullErrMessage: {} \nerrMessage: {} \nerrCode: {} \nHTTPStatusCode: {}",
                    e.getMessage(), e.getErrMsg(), e.getErrorCode(), e.getHttpStatusCode(), e);
        }
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
        return false;
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
        String result = client.market().markPrice(parameters);
        JSONObject jsonObject = new JSONObject(result);
        String markPrice = jsonObject.getString("markPrice");
        logger.info("\n get price market BTCUSDT={}",markPrice,"\n");
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

    public  JSONArray getCurrentAllOpenOrders(){
        LinkedHashMap<String, Object> parameters=new LinkedHashMap<>();
        parameters.put("symbol", Constant.SYMBOL);
        JSONArray allOpenOrders = new JSONArray(client.account().currentAllOpenOrders(parameters));
        return allOpenOrders;
    }

    public List<Integer> getListpricesOpens(String side){
        JSONArray  jsonArray = getCurrentAllOpenOrders();
        if(jsonArray==null || jsonArray.isEmpty()){
            return null;
        }
        List<Integer> listPrice = new ArrayList<>();
        for(int i = 0 ; i < jsonArray.length() ; i ++){
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            String sideOpens = jsonObject.getString("side");
            String price = jsonObject.getString("price");
            if(side.equals(sideOpens) ){
                Integer result = (int)Double.parseDouble(price);
                listPrice.add(result);
            }
        }
        if(Constant.SIDE_BUY.equals(side)){
            Collections.sort(listPrice,new Comparator<Integer>() {
                @Override
                public int compare(Integer d1 , Integer d2){
                    return d2.compareTo(d1);
                }
            });
        }else {
            Collections.sort(listPrice,new Comparator<Integer>() {
                @Override
                public int compare(Integer d1 , Integer d2){
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
