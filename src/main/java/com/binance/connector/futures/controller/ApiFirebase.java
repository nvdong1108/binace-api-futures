package com.binance.connector.futures.controller;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.binance.connector.futures.common.Common;
import com.binance.connector.futures.config.Constant;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.FieldMask;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.WriteResult;
import com.google.firebase.cloud.FirestoreClient;

@Component
public class ApiFirebase {

    private Logger logger = LoggerFactory.getLogger(ApiController.class);


    public Object get(String fieldName){
        Firestore dbFirestore = FirestoreClient.getFirestore();
        FieldMask fieldMask = FieldMask.of(fieldName);
        String collectionName = Constant.SYMBOL + "_constant";
        ApiFuture<DocumentSnapshot> future =  dbFirestore.collection(collectionName).document(fieldName).get(fieldMask);
        try{
            DocumentSnapshot document = future.get();
            if (!document.exists()) {
                return null;
                
            }
            Object beginPricesData = document.getData();
            Map<String,Object> map = convertObjectToHashMap(beginPricesData);
            Object value = map.get(fieldName);
            //long value =Common.convertObectToLong(map.get(fieldName));
            return value;
        }catch(Exception ex){
            ex.printStackTrace();
            return null;
        }
    }
    public Map<String,Object> getDoucment(String orderId){
        Firestore dbFirestore = FirestoreClient.getFirestore();
        String collectionName = Constant.SYMBOL + "_positions";
        DocumentReference documentsReference =  dbFirestore.collection(collectionName).document(orderId);
        try{
            ApiFuture<DocumentSnapshot> future= documentsReference.get();
            return apiFutureToMap(future);
        }catch(Exception ex){
            ex.printStackTrace();
            return null;
        }
    }
    public static Map<String, Object> apiFutureToMap(ApiFuture<DocumentSnapshot> apiFuture) {
        try {
            DocumentSnapshot documentSnapshot = apiFuture.get();
            return documentSnapshotToMap(documentSnapshot);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public static Map<String, Object> documentSnapshotToMap(DocumentSnapshot documentSnapshot) {
        if (documentSnapshot != null && documentSnapshot.exists()) {
            return documentSnapshot.getData();
        } else {
            return null;
        }
    }
    
    public boolean addOrder(String result,String jobSide){
        try{
            Firestore dbFirestore = FirestoreClient.getFirestore();
            Map<String,Object> field = new HashMap<>(); 
            JSONObject  jsonObject = new JSONObject(result);
            String orderId=Common.convertObectToString(jsonObject.get("orderId"));
            int price= Common.convertObectToInt(jsonObject.getString("price")) ;
            String side = jsonObject.getString("side").toLowerCase();
            long time = Common.convertObectToLong(jsonObject.get("updateTime"));
            Date date = new Date(time);
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String dateFormat = simpleDateFormat.format(date);

            Map<String,Object> dataFild = new HashMap<>();
            dataFild.put("id-"+side,orderId);
            dataFild.put("status-"+side,"NEW");
            dataFild.put("price-"+side,price);
            dataFild.put("time-"+side,dateFormat);
            field.put(jobSide+"_"+orderId,dataFild);

            String collectionName = Constant.SYMBOL + "_positions";
            ApiFuture<WriteResult> future =  dbFirestore.collection(collectionName)
            .document(jobSide+"_"+orderId).set(dataFild);
            future.isDone();
            return true;
        }catch(Exception e){
            return false;
        }
    }
    public  boolean addOrder(String orderId, Map<String, Object> dataFild) {
        try{
            Firestore dbFirestore = FirestoreClient.getFirestore();
            Map<String,Object> field = new HashMap<>(); 
            field.put(orderId,dataFild);
            String collectionName = Constant.SYMBOL + "_positions";
            ApiFuture<WriteResult> future =  dbFirestore.collection(collectionName)
            .document(orderId).set(dataFild);
            future.isDone();
            return true;
        }catch(Exception e){
            return false;
        }
    }
    public void add(String fieldName,Object value){
        Firestore dbFirestore = FirestoreClient.getFirestore();
        Map<String,Object> field = new HashMap<>(); 
        field.put(fieldName,value);

        String collectionName = Constant.SYMBOL + "_constant";
        ApiFuture<WriteResult> future =  dbFirestore.collection(collectionName)
        .document(fieldName).set(field);
        future.isDone();
    }
    
    public void addOrderLog(String orderId,Object value,String side){
        Firestore dbFirestore = FirestoreClient.getFirestore();
        Map<String,Object> field = new HashMap<>(); 
        field.put(side+"_"+orderId,value);

        String collectionName = Constant.SYMBOL + "_log";
        ApiFuture<WriteResult> future =  dbFirestore.collection(collectionName)
        .document(side+"_"+orderId).set(field);
        future.isDone();
    }

    public void delete(String orderId,String collectionName){
        Firestore dbFirestore = FirestoreClient.getFirestore();
        DocumentReference documentReference = dbFirestore.collection(collectionName).document(orderId);
        try {
            ApiFuture<WriteResult> future = documentReference.delete();
            future.get();
        } catch (Exception e) {
            e.printStackTrace(); 
        }
        
    }
    public void deleAll(String collectionName,String side){
        Firestore dbFirestore = FirestoreClient.getFirestore();
        try {
            ApiFuture<QuerySnapshot> future = dbFirestore.collection(collectionName).get();
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();
            for (QueryDocumentSnapshot document : documents) {
               String orderId = document.getId();
               if(orderId.contains(side)){
                    delete(orderId,collectionName);
               }
              }
              logger.info("\n\n------>   DELETE ALL Data Firebase Positons Success\n");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public  boolean updateDocumentField(String orderId, Map<String, Object> updates) {
        Firestore dbFirestore = FirestoreClient.getFirestore();

        String collectionName = Constant.SYMBOL + "_positions";
        DocumentReference documentReference = dbFirestore.collection(collectionName).document(orderId);
        try {
            ApiFuture<WriteResult> future = documentReference.update(updates);
            future.get();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    private Map<String, Object> convertObjectToHashMap(Object object) {
        Map<String, Object> hashMap = new HashMap<>();
        if (object instanceof Map) {
            hashMap = (Map<String, Object>) object;
        }
        return hashMap;
    }
   
}
