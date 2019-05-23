package com.symphony.hkex.margincall;

import clients.SymBotClient;
import com.symphony.hkex.margincall.dao.IdmMappingDao;
import listeners.RoomListener;
import model.InboundMessage;
import model.OutboundMessage;
import model.Stream;
import model.events.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RoomListenerTestImpl implements RoomListener {

    private static Logger LOGGER = LoggerFactory.getLogger(RoomListenerTestImpl.class);

    private SymBotClient botClient;

    private IdmMappingDao idmMappingDao;

    public RoomListenerTestImpl(SymBotClient botClient, IdmMappingDao idmMappingDao) {
        this.botClient = botClient;
        this.idmMappingDao = idmMappingDao;
    }

    private final Logger logger = LoggerFactory.getLogger(RoomListenerTestImpl.class);

    public void onRoomMessage(InboundMessage inboundMessage) {
        OutboundMessage messageOut = new OutboundMessage();
        LOGGER.info("Incoming Room Message:\n" + inboundMessage.getMessage());
        if (inboundMessage.getMessage().contains("fund is ready")) {
            String hkexSymphonyID = idmMappingDao.getHKEXChatBotSymphonyID();
            String callID = inboundMessage.getMessage();
            callID = callID.substring(callID.indexOf("fund is ready") + 13);
            callID = callID.substring(0, callID.indexOf("</div>"));
            if (callID.startsWith("D")) {
                messageOut.setMessage("paid " + callID.substring(2));
                try {
                    this.botClient.getMessagesClient().sendMessage(hkexSymphonyID, messageOut);
                } catch (Exception e) {
                    logger.error("onRoomMessage error", e);
                }
            }
        }
    }

    public void onRoomCreated(RoomCreated roomCreated) {

    }

    public void onRoomDeactivated(RoomDeactivated roomDeactivated) {

    }

    public void onRoomMemberDemotedFromOwner(RoomMemberDemotedFromOwner roomMemberDemotedFromOwner) {

    }

    public void onRoomMemberPromotedToOwner(RoomMemberPromotedToOwner roomMemberPromotedToOwner) {

    }

    public void onRoomReactivated(Stream stream) {

    }

    public void onRoomUpdated(RoomUpdated roomUpdated) {

    }

    public void onUserJoinedRoom(UserJoinedRoom userJoinedRoom) {

    }

    public void onUserLeftRoom(UserLeftRoom userLeftRoom) {

    }
}
