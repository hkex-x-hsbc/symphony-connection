package com.symphony.hkex.margincall;

import clients.SymBotClient;
import io.jsonwebtoken.lang.Assert;
import listeners.IMListener;
import model.InboundMessage;
import model.OutboundMessage;
import model.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IMListenerImpl implements IMListener {

    private static Logger LOGGER = LoggerFactory.getLogger(IMListenerImpl.class);

    private SymBotClient botClient;

    public IMListenerImpl(SymBotClient botClient) {
        this.botClient = botClient;
    }

    public void onIMMessage(InboundMessage inboundMessage) {
        String marginCallId = null;
        String partOrGcpName = null;
        String partOrGcpID = null;
        String paymentAmount = null;
        String marginCallType = null;
        String inboundMessageText = inboundMessage.getMessageText();
        try {
            if (inboundMessageText != null) {
                LOGGER.info("Incoming IM Message:\n" + inboundMessageText);
                if (inboundMessageText.indexOf("Margin Call ID:") > -1) {
                    marginCallId = inboundMessageText.substring(inboundMessageText.indexOf("Margin Call ID:") + 15).trim();
                    marginCallType = marginCallId.startsWith("D") ? "D" : "C";
                    marginCallId = marginCallType.equals("C") ? marginCallId.substring(0, marginCallId.indexOf("GCP Name:")).trim() : marginCallId.substring(0, marginCallId.indexOf("Part Name:")).trim();
                }
                Assert.hasText(marginCallId);

                if (marginCallType.equals("C")) {
                    if (inboundMessageText.indexOf("GCP Name:") > -1 && inboundMessageText.indexOf("GCP ID:") > inboundMessageText.indexOf("GCP Name:")) {
                        partOrGcpName = inboundMessageText.substring(inboundMessageText.indexOf("GCP Name:") + 9).trim();
                        partOrGcpName = partOrGcpName.substring(0, partOrGcpName.indexOf("GCP ID:")).trim();
                    }
                } else {
                    if (inboundMessageText.indexOf("Part Name:") > -1 && inboundMessageText.indexOf("Part ID:") > inboundMessageText.indexOf("Part Name:")) {
                        partOrGcpName = inboundMessageText.substring(inboundMessageText.indexOf("Part Name:") + 10).trim();
                        partOrGcpName = partOrGcpName.substring(0, partOrGcpName.indexOf("Part ID:")).trim();
                    }
                }
                Assert.hasText(partOrGcpName);

                if (marginCallType.equals("C")) {
                    if (inboundMessageText.indexOf("GCP ID:") > -1 && inboundMessageText.indexOf("=====================================================================================") > inboundMessageText.indexOf("GCP ID:")) {
                        partOrGcpID = inboundMessageText.substring(inboundMessageText.indexOf("GCP ID:") + 7).trim();
                        partOrGcpID = partOrGcpID.substring(0, partOrGcpID.indexOf("=====================================================================================")).trim();
                    }
                } else {
                    if (inboundMessageText.indexOf("Part ID:") > -1 && inboundMessageText.indexOf("=====================================================================================") > inboundMessageText.indexOf("Part ID:")) {
                        partOrGcpID = inboundMessageText.substring(inboundMessageText.indexOf("Part ID:") + 8).trim();
                        partOrGcpID = partOrGcpID.substring(0, partOrGcpID.indexOf("=====================================================================================")).trim();
                    }
                }

                Assert.hasText(partOrGcpID);

                if (inboundMessageText.indexOf("HK$") > -1) {
                    paymentAmount = inboundMessageText.substring(inboundMessageText.indexOf("HK$") + 3).trim();
                    paymentAmount = marginCallType.equals("C") ? paymentAmount.substring(0, paymentAmount.indexOf("Detail breakdown")).trim() : paymentAmount.substring(0, paymentAmount.indexOf("For more details")).trim();
                }
                Assert.hasText(paymentAmount);
            } else {
                inboundMessageText = inboundMessage.getMessage();
                LOGGER.info("Incoming IM Message:\n" + inboundMessageText);
                if (inboundMessageText.indexOf("Margin Call ID:") > -1) {
                    marginCallId = inboundMessageText.substring(inboundMessageText.indexOf("Margin Call ID:") + 15).trim();
                    marginCallType = marginCallId.startsWith("D") ? "D" : "C";
                    marginCallId = marginCallId.substring(0, marginCallId.indexOf("</p>")).trim();
                }
                Assert.hasText(marginCallId);


                if (marginCallType.equals("C")) {
                    if (inboundMessageText.indexOf("GCP Name:") > -1) {
                        partOrGcpName = inboundMessageText.substring(inboundMessageText.indexOf("GCP Name:") + 9).trim();
                        partOrGcpName = partOrGcpName.substring(0, partOrGcpName.indexOf("</p>")).trim();
                    }
                } else {
                    if (inboundMessageText.indexOf("Part Name:") > -1) {
                        partOrGcpName = inboundMessageText.substring(inboundMessageText.indexOf("GCP Name:") + 10).trim();
                        partOrGcpName = partOrGcpName.substring(0, partOrGcpName.indexOf("</p>")).trim();
                    }
                }

                Assert.hasText(partOrGcpName);

                if (marginCallType.equals("C")) {
                    if (inboundMessageText.indexOf("GCP ID:") > -1) {
                        partOrGcpID = inboundMessageText.substring(inboundMessageText.indexOf("GCP ID:") + 7).trim();
                        partOrGcpID = partOrGcpID.substring(0, partOrGcpID.indexOf("</p>")).trim();
                    }
                } else {
                    if (inboundMessageText.indexOf("Part ID:") > -1) {
                        partOrGcpID = inboundMessageText.substring(inboundMessageText.indexOf("Part ID:") + 8).trim();
                        partOrGcpID = partOrGcpID.substring(0, partOrGcpID.indexOf("</p>")).trim();
                    }
                }

                Assert.hasText(partOrGcpID);

                if (inboundMessageText.indexOf("HK$") > -1) {
                    paymentAmount = inboundMessageText.substring(inboundMessageText.indexOf("HK$") + 3).trim();
                    paymentAmount = paymentAmount.substring(0, paymentAmount.indexOf("</p>")).trim();
                }
                Assert.hasText(paymentAmount);
            }
        } catch (IllegalArgumentException e) {
            LOGGER.warn("invalid message format: ", e);
            return;
        }

        //TODO: insert IDM call record to T_MARGIN_CALL_RECORD
        if (marginCallType.equals("D")) {
            OutboundMessage messageOut = new OutboundMessage();
            String messageOutText = "paid " + marginCallId.substring(marginCallId.indexOf("-") + 1);
            messageOut.setMessage(messageOutText);
            try {
                this.botClient.getMessagesClient().sendMessage(inboundMessage.getStream().getStreamId(), messageOut);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void onIMCreated(Stream stream) {

    }

}
