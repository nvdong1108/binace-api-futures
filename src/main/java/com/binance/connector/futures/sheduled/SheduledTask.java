package com.binance.connector.futures.sheduled;

import com.binance.connector.futures.client.impl.UMFuturesClientImpl;
import com.binance.connector.futures.config.Constant;
import com.binance.connector.futures.config.PrivateConfig;
import com.binance.connector.futures.controller.ApiController;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;

@Component
public class SheduledTask {


    private final static Logger logger = LoggerFactory.getLogger(SheduledTask.class);
    private Double positionAmtCurrent= null;

    @Autowired
    ApiController apiController;

    @Autowired
    MyStartupRunner myStartupRunner;

    LinkedHashMap<String, Object> parameters ; 

    
    @Scheduled(cron = "*/6 * * * * *")
    private void jobOpenBuy() {
        if(myStartupRunner.getResultInitSuccess()){
            openBUY();
        }
    }
    
    @Scheduled(cron = "*/5 * * * * *")
    private void jobOpenSell(){
        if(myStartupRunner.getResultInitSuccess()){
           // openSELL();
        }
    }

    private void openBUY(){
        double markPrice = apiController.getMarkPrice();
        double priceBegin = MyStartupRunner.priceBegin;
        if(markPrice > priceBegin){
            //todo
        }
        double priceDifference = priceBegin - markPrice; 
        
        int difference = Integer.parseInt(String.valueOf(priceDifference))%Integer.parseInt(String.valueOf(Constant.SPACE_PRICE));
        double priceOpensOrder = markPrice+Double.parseDouble(String.valueOf(difference));
        List<Double> openOrders = apiController.getListpricesOpens(Constant.SIDE_BUY);
        int countLoop =Constant.QUANTIYY_OPEN_ORDES+3;
        for(int i = 0 ; i < countLoop; i++){
            priceOpensOrder = priceOpensOrder-Constant.SPACE_PRICE;
            int resul = comparePrice(priceOpensOrder,openOrders.get(i));
            if(i <=Constant.QUANTIYY_OPEN_ORDES){
                if(resul ==0 ){
                    // had opensOrders.
                    continue;
                }
                if (resul==1) {
                    // go opens Orders.
                   apiController.newOrders(priceOpensOrder, Constant.QUANTITY_ONE_EXCHANGE, Constant.SIDE_BUY);
                }else {
                    //todo
                }
            }else if(i>Constant.QUANTIYY_OPEN_ORDES){
                if(resul==1 ){
                   // cancel opens Orders.
                }
            }
        }

    }

    private void openSELL(){
        double sizePositionBegin = MyStartupRunner.priceBegin;
        double sizePositionCurrenr = apiController.getSizePosition();
        double sizePositionDiff = sizePositionBegin - sizePositionCurrenr;
        if(sizePositionDiff <= 0 ){
            return;
        }
        double markPrice = apiController.getMarkPrice();
        double priceBegin = MyStartupRunner.priceBegin;
        if(markPrice > priceBegin){
            //todo
        }
        double priceDifference = priceBegin - markPrice; 
        int difference = Integer.parseInt(String.valueOf(priceDifference))%Integer.parseInt(String.valueOf(Constant.SPACE_PRICE));
        int countAllOpensOrdersSELL = Integer.parseInt(String.valueOf(priceDifference))/Integer.parseInt(String.valueOf(Constant.SPACE_PRICE));
        int countOpenSellRemaining =Integer.parseInt(sizePositionDiff+"")/ Integer.parseInt(""+Constant.QUANTITY_ONE_EXCHANGE); ;
        if (countAllOpensOrdersSELL!=countOpenSellRemaining){
            logger.error("countAllOpensOrdersSELL {} != countOpenSellRemaining {}  " , countAllOpensOrdersSELL,countOpenSellRemaining);
            //todo
        }
        double priceOpensOrder = markPrice+Double.parseDouble(String.valueOf(difference))-Double.valueOf(300);
        List<Double> openOrders = apiController.getListpricesOpens(Constant.SIDE_SELL);
       
        for(int i = 0 ; i < countOpenSellRemaining ; i++){
            priceOpensOrder = priceOpensOrder+Constant.SPACE_PRICE;
            int resul = comparePrice(priceOpensOrder,openOrders.get(i));
            if(resul==1){
                apiController.newOrders(priceOpensOrder, Constant.QUANTITY_ONE_EXCHANGE, Constant.SIDE_SELL);
                countOpenSellRemaining--;
            }
            if(countOpenSellRemaining==0){
                break;
            }
        }
    }

   private int comparePrice(double price , double priceCompare) {
    double spacePrice = Math.abs(price-priceCompare);
    int difference = Integer.parseInt(String.valueOf(spacePrice))%Integer.parseInt(String.valueOf(Constant.SPACE_PRICE));
    if(difference >Constant.PRICE_LIMIT_DIFF){
        return -1;
    }
    int value = Integer.parseInt(String.valueOf(spacePrice))%Integer.parseInt(String.valueOf(Constant.SPACE_PRICE));
    return value;
   }
   

}
