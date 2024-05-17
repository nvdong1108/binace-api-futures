package com.binance.connector.futures.module;

import java.util.Map;

public class NotificationCreateOrderSuccessModule {


    private Map<String,Object> map ;
    private String message ;

    public NotificationCreateOrderSuccessModule(Map<String, Object> map, String message) {
        this.map = map;
        this.message = message;
    }

    public Map<String, Object> getMap() {
        return map;
    }

    public void setMap(Map<String, Object> map) {
        this.map = map;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(message).append(System.lineSeparator());
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            sb.append(entry.getKey()).append("=").append(entry.getValue()).append(System.lineSeparator());
        }
        return sb.toString();
    }
}
