package com.binance.connector.futures.config;

public class Constant {
    public final static int QUANTIYY_OPEN_ORDES = 5;
    public final static double QUANTITY_ONE_EXCHANGE  = 0.002;
   // public final static String BTCUSDT = "ETHUSDT";


    public final static String SYMBOL = "BTCUSDT";

    public final static String SIDE_BUY = "BUY";
    public final static String SIDE_SELL = "SELL";
    public final static String SIDE_FULL = "FULL";


    public final static int  PRICE_LIMIT_DIFF = 10;
    public final static int NOT_FOUND = -1;

    // name document
    public final static String FB_POSITIONS =SYMBOL + "_positions";
    public final static String FB_LOG       =SYMBOL + "_log";
    public final static String FB_CONSTANT  =SYMBOL + "_constant";
}
