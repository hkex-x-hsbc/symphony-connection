package com.hsbc.hkex.margincall;

import authentication.SymBotAuth;
import clients.SymBotClient;
import configuration.SymConfig;
import configuration.SymConfigLoader;
import listeners.RoomListener;
import services.DatafeedEventsService;

import java.net.URL;

public class BotExample {

    public static void main(String [] args) {
        new BotExample();
    }

    public BotExample() {
        URL url = getClass().getResource("config.json");
        SymConfig config = SymConfigLoader.loadFromFile(url.getPath());
        SymBotAuth botAuth = new SymBotAuth(config);
        botAuth.authenticate();
        SymBotClient botClient = SymBotClient.initBot(config, botAuth);
        DatafeedEventsService datafeedEventsService = botClient.getDatafeedEventsService();
        RoomListener roomListenerTest = new RoomListenerTestImpl(botClient);
        datafeedEventsService.addRoomListener(roomListenerTest);
        //IMListener imListener = new IMListenerImpl(botClient);
        //datafeedEventsService.addIMListener(imListener);
    }
}
