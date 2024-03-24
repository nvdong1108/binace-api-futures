package com.binance.connector.futures;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BotAplication {

    public static void main(String[] args) {
        SpringApplication.run(BotAplication.class,args);
    }
}
