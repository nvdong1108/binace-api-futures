package com.binance.connector.futures.sheduled;


import com.binance.connector.futures.config.PrivateConfig;
import com.binance.connector.futures.controller.BotPutMessageLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JobResetParam {


    @Autowired
    BotPutMessageLog botPutMessageLog;
    @Scheduled (cron = "39 39 * * * *")
    public  void reset(){
        try {
            PrivateConfig.resetOrderIdIgnoreBuy();
            PrivateConfig.resetOrderIdIgnoreSell();
            PrivateConfig.resetJsonaArrayTraceListOld();
            botPutMessageLog.post(String.format("Reset param success  %s",new Date()));
        }catch (Exception ex){
            botPutMessageLog.post(String.format("Error JobResetParam.reset : %s",ex.getMessage()));
            ex.printStackTrace();
        }
    }
}
