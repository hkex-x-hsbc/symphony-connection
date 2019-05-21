package com.symphony.hackathon2019.marginbuddy;

import authentication.ISymAuth;
import authentication.SymBotRSAAuth;
import clients.SymBotClient;
import com.google.cloud.dialogflow.v2.DetectIntentResponse;
import com.google.cloud.dialogflow.v2.QueryResult;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import configuration.SymConfig;
import configuration.SymConfigLoader;
import listeners.IMListener;
import listeners.RoomListener;
import lombok.extern.slf4j.Slf4j;
import services.DatafeedEventsService;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.UUID;

@Slf4j
public class BotApp {
    private static final String CLASSPATH = "classpath:";
    public static Config CONFIG = ConfigFactory.load();

    public static String resolvePath(String path) {
        if (path != null && path.startsWith(CLASSPATH)) {
            return BotApp.class.getClassLoader()
                    .getResource(path.substring(CLASSPATH.length()))
                    .getPath();
        }

        return path;
    }

    private static void connectToSymphony() {
        try(InputStream inputStream = new FileInputStream(resolvePath(CONFIG.getString("symphony.config")))) {
            SymConfigLoader configLoader = new SymConfigLoader();
            SymConfig config = configLoader.load(inputStream);
            config.setTruststorePath(resolvePath(config.getTruststorePath()));
            config.setBotPrivateKeyPath(resolvePath(config.getBotPrivateKeyPath()));
            ISymAuth botAuth = new SymBotRSAAuth(config);
            botAuth.authenticate();
            log.info("Connected to Symphony");
            SymBotClient botClient = SymBotClient.initBot(config, botAuth);
            DatafeedEventsService datafeedEventsService = botClient.getDatafeedEventsService();
            RoomListener roomListenerTest = new RoomListenerImpl(botClient);
            datafeedEventsService.addRoomListener(roomListenerTest);
            IMListener imListener = new IMListenerImpl(botClient);
            datafeedEventsService.addIMListener(imListener);
        } catch (Exception e) {
            throw (new RuntimeException("Failed to connect to Symphony", e));
        }
    }

    private static void connectToDialogFlow() {
        QueryResult result = DialogFlowHelper.mapResponse(UUID.randomUUID().toString(), "hi",
                DetectIntentResponse::getQueryResult);
        if (result == null) {
            throw (new RuntimeException("Failed to connect to DialogFlow"));
        }
        log.info("Connected to DialogFlow and got the response: {}", result.getFulfillmentText());
    }

    public static void main(String[] args) {
        connectToSymphony();
        connectToDialogFlow();
    }
}
