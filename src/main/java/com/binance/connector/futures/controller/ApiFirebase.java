package com.binance.connector.futures.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.binance.connector.futures.config.Constant;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.FieldMask;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteResult;
import com.google.firebase.cloud.FirestoreClient;

@Component
public class ApiFirebase {


    private final String COLLECTION_NAME = "prices" ;  
    private final String DOCUMENT_NAME = "prices" ;  

    public int get(String fieldName){
        Firestore dbFirestore = FirestoreClient.getFirestore();
        FieldMask fieldMask = FieldMask.of(fieldName);
        ApiFuture<DocumentSnapshot> future =  dbFirestore.collection(COLLECTION_NAME).document(DOCUMENT_NAME).get(fieldMask);
        try{
            DocumentSnapshot document = future.get();
            if (!document.exists()) {
                return Constant.NOT_FOUND;
                
            }
            Object beginPricesData = document.getData();
            Map<String,Object> map = convertObjectToHashMap(beginPricesData);
            int price = convertObectToInt(map.get(fieldName));
            return price;
        }catch(Exception ex){
            ex.printStackTrace();
            return Constant.NOT_FOUND;

        }
    }

    public void add(String fieldName,Object value){
        Firestore dbFirestore = FirestoreClient.getFirestore();
        Map<String,Object> field = new HashMap<>(); 
        field.put(fieldName,value);
        ApiFuture<WriteResult> future =  dbFirestore.collection(COLLECTION_NAME)
        .document(DOCUMENT_NAME).set(field);

        
    }
    private Map<String, Object> convertObjectToHashMap(Object object) {
        Map<String, Object> hashMap = new HashMap<>();
        if (object instanceof Map) {
            hashMap = (Map<String, Object>) object;
        }
        return hashMap;
    }
    private int convertObectToInt(Object obj){
        if(obj instanceof Integer){
            return (int)obj;
        }
        if(obj instanceof Long){
            Long value = (Long)obj;
            return value.intValue();
        }
        if(obj instanceof String){
            String value = (String)obj;
            return Integer.parseInt(value);
        }
        return Constant.NOT_FOUND;
    }
}
