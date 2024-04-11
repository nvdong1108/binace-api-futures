package com.binance.connector.futures.service;

import java.io.FileInputStream;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

@Service
public class FirebaseInitialization {

    private final static Logger logger = LoggerFactory.getLogger(FirebaseInitialization.class);

    @PostConstruct
    public void initialization(){
        try {
            FileInputStream serviceAccount = new FileInputStream("./key-firebase.json");
    
            FirebaseOptions options = new FirebaseOptions.Builder()
            .setCredentials(GoogleCredentials.fromStream(serviceAccount))
            .build();
            FirebaseApp.initializeApp(options);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
       
    }
    



}
