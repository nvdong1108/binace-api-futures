package com.binance.connector.futures.service;

import java.io.FileInputStream;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

@Service
public class FirebaseInitialization {

    private final static Logger logger = LoggerFactory.getLogger(FirebaseInitialization.class);

    @Autowired
    Environment environment;

    @PostConstruct
    public void initialization(){
        try {

            FileInputStream serviceAccount = null;
            if("prod".equals(environment.getProperty("spring.profiles.active"))){
                serviceAccount=new FileInputStream("./key-firebase.json");
            }else {
                serviceAccount=new FileInputStream("./test-firebase.json");
            }
            @SuppressWarnings("deprecation")
            FirebaseOptions options = new FirebaseOptions.Builder()
            .setCredentials(GoogleCredentials.fromStream(serviceAccount))
            .build();
            FirebaseApp.initializeApp(options);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
       
    }
    



}
