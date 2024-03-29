package com.binance.connector.futures.controller;

import java.text.DecimalFormat;
import java.util.LinkedHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.binance.connector.futures.client.exceptions.BinanceClientException;
import com.binance.connector.futures.client.exceptions.BinanceConnectorException;
import com.binance.connector.futures.client.impl.UMFuturesClientImpl;
import com.binance.connector.futures.config.PrivateConfig;
import com.binance.connector.futures.sheduled.SheduledTask;

@Component
public class ApiOrdersController {

    private final static Logger logger = LoggerFactory.getLogger(SheduledTask.class);
    public void newOrders(double price, double quantity, String side){
        try {
            DecimalFormat decimalFormat = new DecimalFormat("#.##");
            UMFuturesClientImpl client  = new UMFuturesClientImpl(PrivateConfig.TESTNET_API_KEY, PrivateConfig.TESTNET_SECRET_KEY, PrivateConfig.TESTNET_BASE_URL); 
    
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

}
