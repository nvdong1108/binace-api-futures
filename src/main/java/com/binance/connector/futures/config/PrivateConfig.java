package com.binance.connector.futures.config;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public final class PrivateConfig {
    private PrivateConfig() {
    }
    public static final String UM_BASE_URL = "https://fapi.binance.com";
    
    public static final String API_KEY = "afZzAgNGH5Mq1dEx5fguELTbY4JZ5n9pf5WNfAQu8rWDiPQUjVxho3SHZ2BLiDSx";
    public static final String SECRET_KEY = "pjaF2LZ6vRIcpEQMcycGWFIwsc0ctdwRRda7tJ5um22OHR1ztjXX4JwbLF26dRyN";

    public static final String TESTNET_BASE_URL = "https://testnet.binancefuture.com";
    public static final String TESTNET_API_KEY = "0094379c20fe9b9db823f7d6594c7065f89fd34e20064d54b199416ae7c5fbf5";
    public static final String TESTNET_SECRET_KEY = "f78e19252a1bde0958a6986167ef96b9be713d0b2aef8870be88b499f39bf3ac";

    private static List<String> orderIdIgnoreSell = new ArrayList<>();
    private static List<String> orderIdIgnoreBuy= new ArrayList<>();

    private static  JSONArray jsonaArrayTraceListOld= new JSONArray();

    public synchronized static boolean isContentOrderIdIgnoreSell(String orderId) {
        return  orderIdIgnoreSell.contains(orderId);
    }

    public synchronized static void resetOrderIdIgnoreSell() {
        orderIdIgnoreSell = new ArrayList<>();
    }
    public synchronized static void addOrderIdIgnoreSell(String orderId) {
        orderIdIgnoreSell.add(orderId);
    }

    public synchronized static boolean isContentOrderIdIgnoreBuy(String orderId) {
        return  orderIdIgnoreBuy.contains(orderId);
    }

    public synchronized static void resetOrderIdIgnoreBuy() {
       orderIdIgnoreBuy = new ArrayList<>();
    }
    public synchronized static void addOrderIdIgnoreBuy(String orderId) {
        orderIdIgnoreBuy.add(orderId);
    }

    public synchronized static JSONArray getJsonaArrayTraceListOld() {
        return jsonaArrayTraceListOld;
    }

    public synchronized static void resetJsonaArrayTraceListOld() {
        jsonaArrayTraceListOld = new JSONArray();
    }

    public synchronized static boolean isEqualArrayTraceList(JSONArray array1) {
        if (jsonaArrayTraceListOld==null || (array1.length() != jsonaArrayTraceListOld.length())) {
            jsonaArrayTraceListOld = array1;
            return false;
        }
        for (int i = 0; i < array1.length(); i++) {
            try {
                if (!array1.getJSONObject(i).toString().equals(jsonaArrayTraceListOld.getJSONObject(i).toString())) {
                    jsonaArrayTraceListOld = array1;
                    return false;
                }
            } catch (JSONException e) {
                jsonaArrayTraceListOld = array1;
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }
}
