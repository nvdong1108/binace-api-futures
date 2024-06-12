package com.binance.connector.futures.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class BotPutMessageLog {
   public final String GROUP_ID = "-4001382411";
   public final String tokenBot = "6932623557:AAEYQqAgfywj2IKV_AzeRjH1ikv2cYDWbhU";
   public final int MAX_LENGTH_MESSAGE = 4000;

   public void post(String message){
      if(message==null || message.isEmpty()){
         return;
      }
      String apiUrl = "https://api.telegram.org/bot"+tokenBot+"/sendMessage";
      RestTemplate restTemplate = new RestTemplate();
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);
      Map<String,Object> mapEntity = new HashMap<>();
      mapEntity.put("chat_id",GROUP_ID);
      mapEntity.put("text",validateMessage(message));
      HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(mapEntity, headers);
      ResponseEntity<String> responseEntity = restTemplate.postForEntity(apiUrl, requestEntity, String.class);
      String response = responseEntity.getBody();
   }
   private String validateMessage(String message){
      if(message!=null  && message.length()>MAX_LENGTH_MESSAGE){
         return message.substring(0,MAX_LENGTH_MESSAGE);
      }
      return message;
   }

}
