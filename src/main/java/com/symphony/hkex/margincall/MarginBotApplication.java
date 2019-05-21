package com.symphony.hkex.margincall;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MarginBotApplication {

    public static void main(String[] args) {
        SpringApplication.run(MarginBotApplication.class, args);

        BotExample app = new BotExample(); // initial Symphony chatbot
    }
}
