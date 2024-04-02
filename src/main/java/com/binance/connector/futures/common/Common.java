package com.binance.connector.futures.common;

import org.json.JSONArray;
import org.json.JSONObject;

import com.binance.connector.futures.config.Constant;

public class Common {
    public static int comparePrice(int price , int priceCompare) {
    int spacePrice = Math.abs(price-priceCompare);
    int difference = spacePrice%Constant.SPACE_PRICE_INT;
    if(difference >Constant.PRICE_LIMIT_DIFF){
        return -1;
    }
    int value = spacePrice/Constant.SPACE_PRICE_INT;
    return value;
   }

   public static boolean hadOpenOrder(JSONArray jsonArray , int price){
    for(int i = 0 ; i < jsonArray.length() ; i ++){
        JSONObject jsonObject = jsonArray.getJSONObject(i);
        int priceOpens = (int)Double.parseDouble(jsonObject.getString("price"));
        int numSub = Math.abs(price-priceOpens);
        if(numSub==0 || numSub < Constant.PRICE_LIMIT_DIFF){
            return true;
        }
    }
    return false ;
   }
}
