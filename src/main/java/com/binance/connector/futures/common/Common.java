package com.binance.connector.futures.common;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.binance.connector.futures.config.Constant;
import com.binance.connector.futures.sheduled.MyStartupRunner;

public class Common {
    public static int comparePrice(int price , int priceCompare) {
    int spacePrice = Math.abs(price-priceCompare);
    int difference = spacePrice% MyStartupRunner.getSpacePriceInt();;
    if(difference >Constant.PRICE_LIMIT_DIFF){
        return -1;
    }
    int value = spacePrice/MyStartupRunner.getSpacePriceInt();;
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
            return -1;
        }
        if(obj instanceof String || obj instanceof Integer){
            Double valueDouble = Double.parseDouble(obj+"");
            return valueDouble.longValue();
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


    public static boolean isEqual(JSONArray array1, JSONArray array2) {
        if (array1.length() != array2.length()) {
            return false;
        }
        for (int i = 0; i < array1.length(); i++) {
            try {
                if (!array1.getJSONObject(i).toString().equals(array2.getJSONObject(i).toString())) {
                    return false;
                }
            } catch (JSONException e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }
}
