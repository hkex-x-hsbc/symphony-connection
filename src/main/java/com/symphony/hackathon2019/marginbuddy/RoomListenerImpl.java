package com.symphony.hackathon2019.marginbuddy;

import clients.SymBotClient;
import listeners.RoomListener;
import model.InboundMessage;
import model.Stream;
import model.events.*;

public class RoomListenerImpl implements RoomListener {
    public RoomListenerImpl(SymBotClient botClient) {
    }

    @Override
    public void onRoomMessage(InboundMessage inboundMessage) {

    }

    @Override
    public void onRoomCreated(RoomCreated roomCreated) {

    }

    @Override
    public void onRoomDeactivated(RoomDeactivated roomDeactivated) {

    }

    @Override
    public void onRoomMemberDemotedFromOwner(RoomMemberDemotedFromOwner roomMemberDemotedFromOwner) {

    }

    @Override
    public void onRoomMemberPromotedToOwner(RoomMemberPromotedToOwner roomMemberPromotedToOwner) {

    }

    @Override
    public void onRoomReactivated(Stream stream) {

    }

    @Override
    public void onRoomUpdated(RoomUpdated roomUpdated) {

    }

    @Override
    public void onUserJoinedRoom(UserJoinedRoom userJoinedRoom) {

    }

    @Override
    public void onUserLeftRoom(UserLeftRoom userLeftRoom) {

    }
}
