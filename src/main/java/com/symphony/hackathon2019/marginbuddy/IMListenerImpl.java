package com.symphony.hackathon2019.marginbuddy;

import clients.SymBotClient;
import listeners.IMListener;
import model.InboundMessage;
import model.Stream;

public class IMListenerImpl implements IMListener {
    public IMListenerImpl(SymBotClient botClient) {
    }

    @Override
    public void onIMMessage(InboundMessage inboundMessage) {


    }

    @Override
    public void onIMCreated(Stream stream) {

    }
}
