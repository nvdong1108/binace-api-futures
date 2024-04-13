package com.binance.connector.futures.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;
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


    public long get(String fieldName){
        Firestore dbFirestore = FirestoreClient.getFirestore();
        FieldMask fieldMask = FieldMask.of(fieldName);
        ApiFuture<DocumentSnapshot> future =  dbFirestore.collection("constant").document(fieldName).get(fieldMask);
        try{
            DocumentSnapshot document = future.get();
            if (!document.exists()) {
                return Constant.NOT_FOUND;
                
            }
            Object beginPricesData = document.getData();
            Map<String,Object> map = convertObjectToHashMap(beginPricesData);
            long value =Common.convertObectToLong(map.get(fieldName));
            return value;
        }catch(Exception ex){
            ex.printStackTrace();
            return Constant.NOT_FOUND;
        }
    }
    public Map<String,Object> getDoucment(String orderId){
        Firestore dbFirestore = FirestoreClient.getFirestore();
        DocumentReference documentsReference =  dbFirestore.collection("positions").document(orderId);
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
    
    public boolean addOrderId(String result){
        try{
            Firestore dbFirestore = FirestoreClient.getFirestore();
            Map<String,Object> field = new HashMap<>(); 
            JSONObject  jsonObject = new JSONObject(result);
            String orderId=Common.convertObectToString(jsonObject.get("orderId"));
            int price= Common.convertObectToInt(jsonObject.getString("price")) ;
            String side = jsonObject.getString("side").toLowerCase();

            Map<String,Object> dataFild = new HashMap<>();
            dataFild.put("id-"+side,orderId);
            dataFild.put("status-"+side,"NEW");
            dataFild.put("price-"+side,price);
            field.put(orderId,dataFild);
            ApiFuture<WriteResult> future =  dbFirestore.collection("positions")
            .document(orderId).set(dataFild);
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
            ApiFuture<WriteResult> future =  dbFirestore.collection("positions")
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
        ApiFuture<WriteResult> future =  dbFirestore.collection("constant")
        .document(fieldName).set(field);
        future.isDone();
    }
    
    public void addOrderLog(String orderId,Object value){
        Firestore dbFirestore = FirestoreClient.getFirestore();
        Map<String,Object> field = new HashMap<>(); 
        field.put(orderId,value);
        ApiFuture<WriteResult> future =  dbFirestore.collection("LogSuccessOrder")
        .document(orderId).set(field);
        future.isDone();
    }

    public void delete(String orderId){
        Firestore dbFirestore = FirestoreClient.getFirestore();
        DocumentReference documentReference = dbFirestore.collection("positions").document(orderId);
        try {
            ApiFuture<WriteResult> future = documentReference.delete();
            future.get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }
    public void deleAll(){
        Firestore dbFirestore = FirestoreClient.getFirestore();
        try {
            ApiFuture<QuerySnapshot> future = dbFirestore.collection("positions").get();
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();
            for (QueryDocumentSnapshot document : documents) {
               String orderId = document.getId();
               delete(orderId);
              }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public  boolean updateDocumentField(String orderId, Map<String, Object> updates) {
        Firestore dbFirestore = FirestoreClient.getFirestore();
        DocumentReference documentReference = dbFirestore.collection("positions").document(orderId);
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
