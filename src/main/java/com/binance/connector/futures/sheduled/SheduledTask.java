package com.binance.connector.futures.sheduled;

import com.binance.connector.futures.common.Common;
import com.binance.connector.futures.config.Constant;
import com.binance.connector.futures.controller.ApiController;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;

@Component
public class SheduledTask {


    private final static Logger logger = LoggerFactory.getLogger(SheduledTask.class);

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
           openSELL();
        }
    }

    private void openBUY(){
        int markPrice = apiController.getMarkPrice();
        int priceBegin = MyStartupRunner.priceBegin;
        if(markPrice > priceBegin){
            //todo
        }
        int priceDifference = priceBegin - markPrice; 
        
        int difference = priceDifference%Constant.SPACE_PRICE_INT;
        int priceOpensOrder = markPrice+difference;
        List<Integer> openOrders = apiController.getListpricesOpens(Constant.SIDE_BUY);
        int countLoop =openOrders.size()<Constant.QUANTIYY_OPEN_ORDES?Constant.QUANTIYY_OPEN_ORDES:openOrders.size();     
        int countOpensSuccess = 0 ;
        for(int i = 0 ; i < countLoop; i++){
            priceOpensOrder = priceOpensOrder-Constant.SPACE_PRICE_INT;
            if(i<openOrders.size() && i < Constant.QUANTIYY_OPEN_ORDES){
                int resul = Common.comparePrice(priceOpensOrder,openOrders.get(i));
                if(resul ==0 ){
                    // had opensOrders.
                    continue;
                }
                if (resul>=1) {
                    // go opens Orders.
                    apiController.newOrders(priceOpensOrder, Constant.QUANTITY_ONE_EXCHANGE, Constant.SIDE_BUY);
                    countOpensSuccess++;
                }else {
                    //todo
                }
            }else if((i+countOpensSuccess)<countLoop) {
                apiController.newOrders(priceOpensOrder, Constant.QUANTITY_ONE_EXCHANGE, Constant.SIDE_BUY);
            } else if(i > Constant.QUANTIYY_OPEN_ORDES){
                // cancel open order
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
        List<Integer> openOrders = apiController.getListpricesOpens(Constant.SIDE_SELL);
       
        for(int i = 0 ; i < countOpenSellRemaining ; i++){
            priceOpensOrder = priceOpensOrder+Constant.SPACE_PRICE_INT;
            int resul =Common.comparePrice(priceOpensOrder,openOrders.get(i));
            if(resul==1){
                apiController.newOrders(priceOpensOrder, Constant.QUANTITY_ONE_EXCHANGE, Constant.SIDE_SELL);
                countOpenSellRemaining--;
            }
            if(countOpenSellRemaining==0){
                break;
            }
        }
    }

   
   

}
