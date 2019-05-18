package com.hsbc.hkex.margincall;

import clients.SymBotClient;
import listeners.RoomListener;
import model.InboundMessage;
import model.OutboundMessage;
import model.Stream;
import model.events.*;

public class RoomListenerTestImpl implements RoomListener {
    private SymBotClient botClient;

    public RoomListenerTestImpl(SymBotClient botClient) {
        this.botClient = botClient;
    }

    public void onRoomMessage(InboundMessage message) {
        OutboundMessage messageOut = new OutboundMessage();
        messageOut.setMessage("<messageML>Hi "+message.getUser().getFirstName()+"!</messageML>");
        try {
            this.botClient.getMessagesClient().sendMessage(message.getStream().getStreamId(), messageOut);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onRoomCreated(RoomCreated roomCreated) {}

    public void onRoomDeactivated(RoomDeactivated roomDeactivated) {}

    public void onRoomMemberDemotedFromOwner(RoomMemberDemotedFromOwner roomMemberDemotedFromOwner) {}

    public void onRoomMemberPromotedToOwner(RoomMemberPromotedToOwner roomMemberPromotedToOwner) {}

    public void onRoomReactivated(Stream stream) {}

    public void onRoomUpdated(RoomUpdated roomUpdated) {}

    public void onUserJoinedRoom(UserJoinedRoom userJoinedRoom) {
        OutboundMessage messageOut = new OutboundMessage();
        messageOut.setMessage("<messageML>Welcome "+userJoinedRoom.getAffectedUser().getFirstName()+"!</messageML>");
        try {
            this.botClient.getMessagesClient().sendMessage(userJoinedRoom.getStream().getStreamId(), messageOut);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onUserLeftRoom(UserLeftRoom userLeftRoom) {}
}
