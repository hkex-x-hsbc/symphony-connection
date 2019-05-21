package com.symphony.hackathon2019.marginbuddy;

import clients.SymBotClient;
import listeners.IMListener;
import lombok.extern.slf4j.Slf4j;
import model.InboundMessage;
import model.OutboundMessage;
import model.Stream;

@Slf4j
public class IMListenerImpl implements IMListener {
    private final SymBotClient botClient;

    public IMListenerImpl(SymBotClient botClient) {
        this.botClient = botClient;
    }

    @Override
    public void onIMMessage(InboundMessage inboundMessage) {
        String sessionId = inboundMessage.getStream().getStreamId();
        String text = inboundMessage.getMessageText();
        DialogFlowHelper.consumeResponse(sessionId, text, (response -> {
            OutboundMessage message = new OutboundMessage(response.getQueryResult().getFulfillmentText());
            botClient.getMessagesClient().sendMessage(sessionId, message);
        }));
    }

    @Override
    public void onIMCreated(Stream stream) {
        log.info(stream.toString());
    }
}
