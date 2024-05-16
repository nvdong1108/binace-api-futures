package com.binance.connector.futures.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.binance.connector.futures.common.Common;
import com.binance.connector.futures.config.Constant;
import com.binance.connector.futures.sheduled.MyStartupRunner;
import com.google.api.Http;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping("/api")
public class RestAPIController {

    Logger logger = LoggerFactory.getLogger(RestAPIController.class);

    @Autowired
    ApiController apiController;

    @Autowired
    ApiFirebase firebase;

    @PostMapping("/createOrder")
    public ResponseEntity<Object> createOrder(@RequestBody Map<String,Object> requestBody) {
        logger.info("***** API Create New order Begin ***** ");
        int priceOpenOrder = Common.convertObectToInt(requestBody.get("price"));
        String side = Common.convertObectToString(requestBody.get("side"));
        if(priceOpenOrder==0 || priceOpenOrder==-1 || side ==null || side.isEmpty() ){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid Request Body");
        }
         try {
            String  result = apiController.newOrders(priceOpenOrder,Constant.QUANTITY_ONE_EXCHANGE,side);
            if(result.equalsIgnoreCase("-2019")){
                Map<String,Object> mstObj = new HashMap<>();
                mstObj.put("code", "-2019");
                mstObj.put("msg", "Margin is insufficient");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(mstObj);
            }
            firebase.addOrderStatusNew(result,side);
            JSONObject jsonObject = new JSONObject(result);
            String orderId =Common.convertObectToString(jsonObject.get("orderId"));
            logger.info("***** SUCCESS OrderId = {} ***** ",orderId);
            return ResponseEntity.ok(" Create Success "+ orderId);
         } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
         }
    }

    @PostMapping("/deleteLog")
    public ResponseEntity<Object> deleteLog(@RequestBody Map<String,Object> requestBody) {
        try {
            firebase.deleAll(Constant.FB_LOG,"BUY");
            firebase.deleAll(Constant.FB_POSITIONS,"BUY");
            firebase.deleAll(Constant.FB_LOG,"SELL");
            firebase.deleAll(Constant.FB_POSITIONS,"SELL");     
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
        return ResponseEntity.ok(" delete Success ");
    }
    @PostMapping("/test")
    public ResponseEntity<Object> test(@RequestBody Map<String,Object> requestBody) {
        try {
            String messsage = requestBody.get("message").toString();
            BotPutMessageLog botPutMessageLog = new BotPutMessageLog();
            botPutMessageLog.post(messsage);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
        return ResponseEntity.ok(" delete Success ");
    }


}
