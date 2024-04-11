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

   public static String convertObectToString(Object obj){
    if(obj==null){
        return null;
    }
    return obj+"";
    }
    public static long convertObectToLong(Object obj){
        if(obj==null){
            return 0;
        }
        if(obj instanceof String || obj instanceof Integer){
            return Long.parseLong(obj+"");
        }
        return (long)obj;
    }
    public static int convertObectToInt(Object obj){
        if(obj==null){
            return 0;
        }
        if(obj instanceof String ){
            Double _double= Double.parseDouble(obj+"");
            return _double.intValue();
        }
        if(obj instanceof Long){
            Long _value = (Long)obj;
            return _value.intValue();
        }
        return (int)obj;
    }
}
