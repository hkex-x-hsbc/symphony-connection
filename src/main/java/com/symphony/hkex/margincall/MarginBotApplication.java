package com.symphony.hkex.margincall;

import com.symphony.hkex.margincall.dao.IdmMappingDao;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class MarginBotApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(MarginBotApplication.class, args);

        BotExample.initialize(context.getBean(IdmMappingDao.class)); // initial Symphony chatbot
    }
}
