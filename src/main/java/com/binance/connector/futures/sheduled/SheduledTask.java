package com.binance.connector.futures.sheduled;

import com.binance.connector.futures.common.Common;
import com.binance.connector.futures.config.Constant;
import com.binance.connector.futures.controller.ApiController;

import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;

@Component
public class SheduledTask {


    private final static Logger logger = LoggerFactory.getLogger(SheduledTask.class);

    @Autowired
    ApiController apiController;

    @Autowired
    MyStartupRunner myStartupRunner;

    LinkedHashMap<String, Object> parameters ; 

    boolean paramFlagRunOneTime = true ;
    
    @Scheduled(fixedDelay = 7000)
    private void jobOpenBuy() {
        if(myStartupRunner.getResultInitSuccess()){
            openBUY();
            if(paramFlagRunOneTime){
                myStartupRunner.setResultCreateBuySuccess(true);
                paramFlagRunOneTime=false;
            }
        }
    }
    
    @Scheduled(fixedDelay = 5000)
    private void jobOpenSell(){
        if(myStartupRunner.getResultRunJobSell()){
           openSELL();
        }
    }

    private void openBUY(){
        int markPrice = apiController.getMarkPrice();
        int priceBegin = MyStartupRunner.priceBegin;
       
        JSONArray jsonArray = apiController.getCurrentAllOpenOrders(Constant.SIDE_BUY);
        if( jsonArray!=null){
            if(jsonArray.length()>=Constant.QUANTIYY_OPEN_ORDES){
                return;
            }
        }
        int priceOpenOrders=0;
        
        if(priceBegin<markPrice){
            priceOpenOrders  = priceBegin;
        }else {
            priceOpenOrders = ((priceBegin-markPrice)%Constant.SPACE_PRICE_INT) + markPrice;
        }
        for(int i = 0 ; i < Constant.MAX_OPEN_ORDES ; i ++){
            priceOpenOrders=priceOpenOrders-Constant.SPACE_PRICE_INT;
            boolean result = false;
            if(jsonArray !=null && !jsonArray.isEmpty()){
                result = Common.hadOpenOrder(jsonArray, priceOpenOrders);
            }
            if(result){
                continue;
            }
            apiController.newOrders(priceOpenOrders, Constant.QUANTITY_ONE_EXCHANGE, Constant.SIDE_BUY);

            jsonArray = apiController.getCurrentAllOpenOrders(Constant.SIDE_BUY); 
            if( jsonArray!=null){
                if(jsonArray.length()>=Constant.QUANTIYY_OPEN_ORDES){
                    return;
                }
            }
        }
        logger.debug( "***************  final function job buy ***************");

    }
    private void openSELL(){
        double sizePositionBegin = MyStartupRunner.sizePositionBegin;
        double sizePositionCurrenr = apiController.getSizePosition();
        double sizePositionDiff =sizePositionCurrenr-sizePositionBegin;
        if(sizePositionDiff <= 0 ){
            return;
        }
        int markPrice = apiController.getMarkPrice();
        int priceBegin = MyStartupRunner.priceBegin;
        if(markPrice > priceBegin){
            //todo
        }
        int priceDifference = priceBegin - markPrice; 
        int difference = priceDifference%Constant.SPACE_PRICE_INT;
        int countAllOpensOrdersSELL = priceDifference/Constant.SPACE_PRICE_INT;
        int countOpenSellRemaining =(int)(sizePositionDiff/Constant.QUANTITY_ONE_EXCHANGE); ;
        if (countAllOpensOrdersSELL!=countOpenSellRemaining){
            logger.error("countAllOpensOrdersSELL {} != countOpenSellRemaining {}  " , countAllOpensOrdersSELL,countOpenSellRemaining);
            //todo
        }
        int priceOpensOrder = markPrice+difference-Constant.SPACE_PRICE_INT;
        JSONArray jsonArray = apiController.getCurrentAllOpenOrders(Constant.SIDE_SELL);
       
        for(int i = 0 ; i < countAllOpensOrdersSELL ; i++){
            priceOpensOrder = priceOpensOrder+Constant.SPACE_PRICE_INT;
            boolean resul=false;
            if(jsonArray!=null && !jsonArray.isEmpty()){
                resul=Common.hadOpenOrder(jsonArray,priceOpensOrder);
            }
            if(resul){
                continue;
            }
            apiController.newOrders(priceOpensOrder, Constant.QUANTITY_ONE_EXCHANGE, Constant.SIDE_SELL);
            countOpenSellRemaining--;
        }
    }

    

   
   

}
