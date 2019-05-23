package com.symphony.hkex.margincall;

import authentication.ISymAuth;
import authentication.SymBotRSAAuth;
import clients.SymBotClient;
import com.symphony.hkex.margincall.dao.IdmMappingDao;
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

    private static boolean initialized = false;

    private BotExample() {
    }

    public static void initialize(IdmMappingDao idmMappingDao) {
        if (initialized) {
            return;
        }
        BasicConfigurator.configure();

        SymConfigLoader configLoader = new SymConfigLoader();
        SymConfig config = configLoader.load(idmMappingDao.getClass().getClassLoader().getResourceAsStream("config.json"));
        config.setTruststorePath(config.getTruststorePath().startsWith("classpath") ? config.getTruststorePath().substring(10) : config.getTruststorePath());
        config.setBotPrivateKeyPath(config.getBotPrivateKeyPath().startsWith("classpath") ? config.getBotPrivateKeyPath().substring(10) : config.getBotPrivateKeyPath());
        ISymAuth botAuth = new SymBotRSAAuth(config);
        botAuth.authenticate();
        SymBotClient botClient = SymBotClient.initBot(config, botAuth);
        DatafeedEventsService datafeedEventsService = botClient.getDatafeedEventsService();
        RoomListener roomListenerTest = new RoomListenerTestImpl(botClient, idmMappingDao);
        datafeedEventsService.addRoomListener(roomListenerTest);
        IMListener imListener = new IMListenerImpl(botClient, idmMappingDao);
        datafeedEventsService.addIMListener(imListener);
        //createRoom(botClient);
        initialized = true;
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
