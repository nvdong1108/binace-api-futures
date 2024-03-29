package com.binance.connector.futures.sheduled;

import java.util.LinkedHashMap;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.binance.connector.futures.client.impl.UMFuturesClientImpl;
import com.binance.connector.futures.config.PrivateConfig;
import com.binance.connector.futures.controller.ApiOrdersController;

import jakarta.annotation.PostConstruct;

@Component
public class MyStartupRunner {

    private final static Logger logger = LoggerFactory.getLogger(MyStartupRunner.class);

    public boolean initSuccess = false;

    private Double priceBegin = null;

    private double SPACE_PRICE = 300;

    private int QUANTIYY_OPEN_ORDES = 10;

    UMFuturesClientImpl client ;

    @Autowired
    ApiOrdersController apiOrdersContronller;

    @PostConstruct
    public void init() {
        client =  new UMFuturesClientImpl(PrivateConfig.TESTNET_API_KEY, PrivateConfig.TESTNET_SECRET_KEY, PrivateConfig.TESTNET_BASE_URL);
        getPriceBegin();
        cancelAllOpenOrders();
        openAllOrder();
    }

    public Double getPriceBegin(){
        LinkedHashMap<String, Object> parameters = new LinkedHashMap<>();
        parameters.put("symbol", "BTCUSDT");
        String result = client.market().markPrice(parameters);
        JSONObject jsonObject = new JSONObject(result);
        String markPrice = jsonObject.getString("markPrice");
        priceBegin = Double.parseDouble(markPrice);
        logger.info("\n get price market BTCUSDT={}",priceBegin,"\n");
        return Double.parseDouble(markPrice);
    }

    private void openAllOrder(){
        double priceOpenOrder = priceBegin;
        for(int i = 0 ; i < QUANTIYY_OPEN_ORDES ; i ++){
            priceOpenOrder = priceOpenOrder - SPACE_PRICE;
            apiOrdersContronller.newOrders(priceOpenOrder,0.01, "BUY");
        }
        setResultInitSuccess(true);
    }

    private void cancelAllOpenOrders(){
        LinkedHashMap<String, Object> parameters = new LinkedHashMap<>();
        parameters.put("symbol", "BTCUSDT");
        String result = client.account().cancelAllOpenOrders(parameters);
        logger.info("\n Cancel ALL Orders Success {}",result + "\n");
        
    }

    public boolean getResultInitSuccess(){
        return this.initSuccess;
    }

    public void setResultInitSuccess(boolean result){
        this.initSuccess =result;
    }
}
