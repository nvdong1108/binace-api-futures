package com.binance.connector.futures.sheduled;


import com.binance.connector.futures.config.Constant;
import com.binance.connector.futures.config.PrivateConfig;
import com.binance.connector.futures.controller.ApiController;
import com.binance.connector.futures.controller.BotPutMessageLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.Console;
import java.util.Date;

@Component
public class JobResetParam {


    @Autowired
    BotPutMessageLog botPutMessageLog;

    @Autowired
    MyStartupRunner myStartupRunner;

    @Autowired
    ApiController apiController;

    @Scheduled (cron = "39 39 * * * *")
    public  void reset(){
        try {
            if(myStartupRunner.isRunBotSell()){
                PrivateConfig.resetOrderIdIgnoreBuy();
                PrivateConfig.resetOrderIdIgnoreSell();
                PrivateConfig.resetJsonaArrayTraceListOld();
                long price= apiController.getPriceCurrent(Constant.SYMBOL);
                botPutMessageLog.post(String.format(" Notification Price %s = %s",Constant.SYMBOL,price));
            }
        }catch (Exception ex){
            botPutMessageLog.post(String.format("Error JobResetParam.reset : %s",ex.getMessage()));
            ex.printStackTrace();
        }
    }
}
