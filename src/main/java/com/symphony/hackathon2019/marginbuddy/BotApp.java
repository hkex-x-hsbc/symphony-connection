package com.symphony.hackathon2019.marginbuddy;

import authentication.ISymAuth;
import authentication.SymBotRSAAuth;
import clients.SymBotClient;
import configuration.SymConfig;
import configuration.SymConfigLoader;
import listeners.IMListener;
import listeners.RoomListener;
import lombok.extern.slf4j.Slf4j;
import services.DatafeedEventsService;

import java.io.FileInputStream;
import java.io.InputStream;

@Slf4j
public class BotApp {

    public static final String CLASSPATH = "classpath:";

    private static String resolvePath(String path) {
        if (path != null && path.startsWith(CLASSPATH)) {
            return BotApp.class.getClassLoader()
                    .getResource(path.substring(CLASSPATH.length()))
                    .getPath();
        }

        return path;
    }

    public static void main(String[] args) {
        try(InputStream inputStream = new FileInputStream(resolvePath("classpath:config.json"))) {
            SymConfigLoader configLoader = new SymConfigLoader();
            SymConfig config = configLoader.load(inputStream);
            config.setTruststorePath(resolvePath(config.getTruststorePath()));
            config.setBotPrivateKeyPath(resolvePath(config.getBotPrivateKeyPath()));
            ISymAuth botAuth = new SymBotRSAAuth(config);
            botAuth.authenticate();
            SymBotClient botClient = SymBotClient.initBot(config, botAuth);
            DatafeedEventsService datafeedEventsService = botClient.getDatafeedEventsService();
            RoomListener roomListenerTest = new RoomListenerImpl(botClient);
            datafeedEventsService.addRoomListener(roomListenerTest);
            IMListener imListener = new IMListenerImpl(botClient);
            datafeedEventsService.addIMListener(imListener);
        } catch (Exception e) {
            log.error("Failed to connect to develop2.symphony.com", e);
        }
    }
}
