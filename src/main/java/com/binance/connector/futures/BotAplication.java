package com.binance.connector.futures;


import java.util.Collections;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BotAplication {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(BotAplication.class);
        app.setDefaultProperties(Collections
          .singletonMap("server.port", "8083"));
          app.run(args);
        // SpringApplication.run(BotAplication.class,args);
    }
}
