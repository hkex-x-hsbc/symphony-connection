package com.symphony.hkex.margincall;

import authentication.ISymAuth;
import authentication.SymBotRSAAuth;
import clients.SymBotClient;
import configuration.SymConfig;
import configuration.SymConfigLoader;
import listeners.IMListener;
import listeners.RoomListener;
import model.*;
import org.apache.log4j.BasicConfigurator;
import services.DatafeedEventsService;

import javax.ws.rs.core.NoContentException;
import java.util.List;

public class BotExample {

    public static void main(String[] args) {
        BotExample app = new BotExample();
    }


    public BotExample() {
        BasicConfigurator.configure();

        SymConfigLoader configLoader = new SymConfigLoader();
        SymConfig config = configLoader.load(this.getClass().getClassLoader().getResourceAsStream("config.json"));
        config.setTruststorePath(config.getTruststorePath().startsWith("classpath") ? getClass().getClassLoader().getResource(config.getTruststorePath().substring(10)).getPath() : config.getTruststorePath());
        config.setBotPrivateKeyPath(config.getBotPrivateKeyPath().startsWith("classpath") ? getClass().getClassLoader().getResource(config.getBotPrivateKeyPath().substring(10)).getPath() : config.getBotPrivateKeyPath());
        ISymAuth botAuth = new SymBotRSAAuth(config);
        botAuth.authenticate();
        SymBotClient botClient = SymBotClient.initBot(config, botAuth);
        DatafeedEventsService datafeedEventsService = botClient.getDatafeedEventsService();
        RoomListener roomListenerTest = new RoomListenerTestImpl(botClient);
        datafeedEventsService.addRoomListener(roomListenerTest);
        IMListener imListener = new IMListenerImpl(botClient);
        datafeedEventsService.addIMListener(imListener);
        //createRoom(botClient);

    }

    private void createRoom(SymBotClient botClient) {


        try {

            UserInfo userInfo = botClient.getUsersClient().getUserFromEmail("manuela.caicedo@example.com", true);
            //get user IM and send message
            String IMStreamId = botClient.getStreamsClient().getUserIMStreamId(userInfo.getId());
            OutboundMessage message = new OutboundMessage();
            message.setMessage("test IM");
            botClient.getMessagesClient().sendMessage(IMStreamId, message);

            Room room = new Room();
            room.setName("test room preview");
            room.setDescription("test");
            room.setDiscoverable(true);
            room.setPublic(true);
            room.setViewHistory(true);
            RoomInfo roomInfo = null;
            roomInfo = botClient.getStreamsClient().createRoom(room);
            botClient.getStreamsClient().addMemberToRoom(roomInfo.getRoomSystemInfo().getId(), userInfo.getId());

            Room newRoomInfo = new Room();
            newRoomInfo.setName("test generator");
            botClient.getStreamsClient().updateRoom(roomInfo.getRoomSystemInfo().getId(), newRoomInfo);

            List<RoomMember> members = botClient.getStreamsClient().getRoomMembers(roomInfo.getRoomSystemInfo().getId());

            botClient.getStreamsClient().promoteUserToOwner(roomInfo.getRoomSystemInfo().getId(), userInfo.getId());

            botClient.getStreamsClient().deactivateRoom(roomInfo.getRoomSystemInfo().getId());


        } catch (NoContentException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
