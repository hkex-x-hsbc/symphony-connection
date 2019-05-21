package com.symphony.hkex.margincall;

import clients.SymBotClient;
import io.jsonwebtoken.lang.Assert;
import listeners.IMListener;
import model.InboundMessage;
import model.OutboundMessage;
import model.Stream;

public class IMListenerImpl implements IMListener {

    private SymBotClient botClient;

    public IMListenerImpl(SymBotClient botClient) {
        this.botClient = botClient;
    }

    public void onIMMessage(InboundMessage inboundMessage) {
        String marginCallId = null;
        String partOrGcpName = null;
        String partOrGcpID = null;
        String paymentAmount = null;
        String inboundMessageText = inboundMessage.getMessageText();
        if(inboundMessageText!=null) {
            if (inboundMessageText.indexOf("Margin Call ID:") > -1 && inboundMessageText.indexOf("GCP Name:") > inboundMessageText.indexOf("Margin Call ID:")) {
                marginCallId = inboundMessageText.substring(inboundMessageText.indexOf("Margin Call ID:") + 15);
                marginCallId = marginCallId.substring(0, marginCallId.indexOf("GCP Name:"));
                marginCallId = marginCallId.trim();
            }
            Assert.hasText(marginCallId);

            if (inboundMessageText.indexOf("GCP Name:") > -1 && inboundMessageText.indexOf("GCP ID:") > inboundMessageText.indexOf("GCP Name:")) {
                partOrGcpName = inboundMessageText.substring(inboundMessageText.indexOf("GCP Name:") + 9);
                partOrGcpName = partOrGcpName.substring(0, partOrGcpName.indexOf("GCP ID:"));
                partOrGcpName = partOrGcpName.trim();
            }
            Assert.hasText(partOrGcpName);

            if (inboundMessageText.indexOf("GCP ID:") > -1 && inboundMessageText.indexOf("=====================================================================================") > inboundMessageText.indexOf("GCP ID:")) {
                partOrGcpID = inboundMessageText.substring(inboundMessageText.indexOf("GCP ID:") + 7);
                partOrGcpID = partOrGcpID.substring(0, partOrGcpID.indexOf("====================================================================================="));
                partOrGcpID = partOrGcpID.trim();
            }
            Assert.hasText(partOrGcpID);

            if (inboundMessageText.indexOf("HK$") > -1 && inboundMessageText.indexOf("Detail breakdown") > inboundMessageText.indexOf("HK$")) {
                paymentAmount = inboundMessageText.substring(inboundMessageText.indexOf("HK$") + 3);
                paymentAmount = paymentAmount.substring(0, paymentAmount.indexOf("Detail breakdown"));
                paymentAmount = paymentAmount.trim();
            }
            Assert.hasText(paymentAmount);
        } else {
            inboundMessageText = inboundMessage.getMessage();
            if (inboundMessageText.indexOf("Margin Call ID:") > -1) {
                marginCallId = inboundMessageText.substring(inboundMessageText.indexOf("Margin Call ID:") + 15);
                marginCallId = marginCallId.substring(0, marginCallId.indexOf("</p>"));
                marginCallId = marginCallId.trim();
            }
            Assert.hasText(marginCallId);

            if (inboundMessageText.indexOf("GCP Name:") > -1) {
                partOrGcpName = inboundMessageText.substring(inboundMessageText.indexOf("GCP Name:") + 9);
                partOrGcpName = partOrGcpName.substring(0, partOrGcpName.indexOf("</p>"));
                partOrGcpName = partOrGcpName.trim();
            }
            Assert.hasText(partOrGcpName);

            if (inboundMessageText.indexOf("GCP ID:") > -1) {
                partOrGcpID = inboundMessageText.substring(inboundMessageText.indexOf("GCP ID:") + 7);
                partOrGcpID = partOrGcpID.substring(0, partOrGcpID.indexOf("</p>"));
                partOrGcpID = partOrGcpID.trim();
            }
            Assert.hasText(partOrGcpID);

            if (inboundMessageText.indexOf("HK$") > -1) {
                paymentAmount = inboundMessageText.substring(inboundMessageText.indexOf("HK$") + 3);
                paymentAmount = paymentAmount.substring(0, paymentAmount.indexOf("</p>"));
                paymentAmount = paymentAmount.trim();
            }
            Assert.hasText(paymentAmount);
        }
        //TODO: insert IDM call record to T_MARGIN_CALL_RECORD
        OutboundMessage messageOut = new OutboundMessage();
        String outboundMessageTemplate = "<b>Margin Call ID: #System-#workflowId</b><br/>" +
                "<b>#partOrGcpName</b><br/>" +
                "<b>#partOrGcpID</b><br/>" +
                "========================<br/>" +
                "#paymentStatus<br/>" +
                "========================";
        String messageOutText = outboundMessageTemplate.replace("#System-#workflowId", marginCallId).replace("#partOrGcpName", partOrGcpName).replace("#partOrGcpID", partOrGcpID).replace("#paymentStatus", "Payment fund ready");
        messageOut.setMessage(messageOutText);
        try {
            this.botClient.getMessagesClient().sendMessage(inboundMessage.getStream().getStreamId(), messageOut);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onIMCreated(Stream stream) {

    }

}
