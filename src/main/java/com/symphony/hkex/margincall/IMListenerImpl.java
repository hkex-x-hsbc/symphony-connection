package com.symphony.hkex.margincall;

import clients.SymBotClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.symphony.hkex.margincall.dao.IdmMappingDao;
import io.jsonwebtoken.lang.Assert;
import listeners.IMListener;
import model.InboundMessage;
import model.OutboundMessage;
import model.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.util.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IMListenerImpl implements IMListener {

    private static Logger LOGGER = LoggerFactory.getLogger(IMListenerImpl.class);

    private SymBotClient botClient;

    private IdmMappingDao idmMappingDao;

    public IMListenerImpl(SymBotClient botClient, IdmMappingDao idmMappingDao) {
        this.botClient = botClient;
        this.idmMappingDao = idmMappingDao;
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
                        partOrGcpName = inboundMessageText.substring(inboundMessageText.indexOf("Part Name:") + 10).trim();
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
            LOGGER.debug("invalid message format: ", e);
            return;
        }

        idmMappingDao.saveHKEXChatBotSymphonyID(inboundMessage.getStream().getStreamId());

        if (marginCallType.equals("D")) {
            String symphonyId = idmMappingDao.getSymphonyIDForCallTypeD();
            Map<String, String> report = new HashMap<>();
            report.put("callID", marginCallId.substring(marginCallId.indexOf("-") + 1));
            report.put("partID", partOrGcpID);
            report.put("partName", partOrGcpName);
            report.put("paymentAmount", paymentAmount);
            String dueDate = new Date(new Date().getTime() + 3600 * 1000).toString();
            try {
                String roomMessageOut = formatDMessage(partOrGcpID, marginCallId, paymentAmount, dueDate, "", "#FF0000");
                OutboundMessage outboundMessage = new OutboundMessage();
                outboundMessage.setMessage(roomMessageOut);
                this.botClient.getMessagesClient().sendMessage(symphonyId, outboundMessage);
            } catch (Exception e) {
                LOGGER.error("Fail to route to room type D", e);
            }
        } else {
            String originMessageText = inboundMessage.getMessageText();
            String amountTable = originMessageText.substring(originMessageText.indexOf("StockQuantityMarks amount") + 25);
            amountTable = amountTable.substring(0, amountTable.indexOf("For more details")).trim();
            String[] amountTableArray = amountTable.split("\\s+");
            Map<String, String> amountTableMap = new HashMap<>();
            List<String> stockCodeList = new ArrayList<>();
            for (int i = 0; i < amountTableArray.length; i++) {
                String marksAmount = amountTableArray[i].trim().substring(5);
                marksAmount = marksAmount.substring(marksAmount.indexOf("-"));
                amountTableMap.put(amountTableArray[i].trim().substring(0, 5), marksAmount);
                stockCodeList.add(amountTableArray[i].trim().substring(0, 5));
            }
            List<Map<String, Object>> symphonyIds = idmMappingDao.getSymphonyIDForCallTypeC(stockCodeList.toArray(new String[stockCodeList.size()]));
            for (int j = 0; j < symphonyIds.size(); j++) {
                String stockCode = String.valueOf(symphonyIds.get(j).get("STOCK_CODE"));
                String symphonyId = String.valueOf(symphonyIds.get(j).get("SYMPHONY_ID"));
                Map<String, String> report = new HashMap<>();
                report.put("callID", marginCallId.substring(marginCallId.indexOf("-") + 1));
                report.put("stockCode", stockCode);
                report.put("paymentAmount", amountTableMap.get(stockCode));
                String dueDate = new Date(new Date().getTime() + 3600 * 1000).toString();
                String colour = "green";
                if (report.get("paymentAmount").equalsIgnoreCase("-16,000")) {
                    colour = "yellow";
                }
                try {
                    String roomMessageOut = formatDMessage(partOrGcpID, marginCallId, report.get("paymentAmount"), dueDate, stockCode, colour);
                    OutboundMessage outboundMessage = new OutboundMessage();
                    outboundMessage.setMessage(roomMessageOut);
                    this.botClient.getMessagesClient().sendMessage(symphonyId, outboundMessage);
                } catch (Exception e) {
                    LOGGER.error("Fail to route to room type D", e);
                }
            }
        }

        //TODO: insert IDM call record to T_MARGIN_CALL_RECORD
//        if (marginCallType.equals("D")) {
//            OutboundMessage messageOut = new OutboundMessage();
//            String messageOutText = "paid " + marginCallId.substring(marginCallId.indexOf("-") + 1);
//            messageOut.setMessage(messageOutText);
//            try {
//                this.botClient.getMessagesClient().sendMessage(inboundMessage.getStream().getStreamId(), messageOut);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
    }

    public void onIMCreated(Stream stream) {

    }

    public static String formatDMessage(String partId, String marginCallId, String paymentAmount, String dueDate, String stockCode, String colour) {
        return "Hi there,<br /> Please find Intra-Day Margin Call report as below<br /><br />" +
                "<table>" +
                "<tr>" +
                "<td><b>Broker ID</b></td>" +
                "<td><b>Call ID</b></td>" +
                "<td><b>Amount</b></td>" +
                "<td><b>Due Time</b></td>" +
                "</tr>" +
                "<tr>" +
                "<td>" + partId + "</td>" +
                "<td>" + marginCallId + "</td>" +
                "<td style='color:" + colour + "'>" + paymentAmount + "</td>" +
                "<td>" + dueDate + "</td>" +
                "</tr>" +
                "</table>" +
                "<br/>" +
                "<div style='display:none'>" + stockCode + "</div>" +
                "Please prepare the require fund amount by the due time.<br/>" +
                "If you have any questions, please feel free to contact us at +852 2288 1234";
    }

}
