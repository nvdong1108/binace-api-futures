package com.binance.connector.futures.common;

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
}
