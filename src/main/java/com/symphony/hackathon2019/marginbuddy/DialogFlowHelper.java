package com.symphony.hackathon2019.marginbuddy;

import com.google.cloud.dialogflow.v2.*;
import lombok.extern.slf4j.Slf4j;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.google.auth.oauth2.ServiceAccountCredentials.fromStream;
import static com.symphony.hackathon2019.marginbuddy.BotApp.CONFIG;
import static com.symphony.hackathon2019.marginbuddy.BotApp.resolvePath;

@Slf4j
public class DialogFlowHelper {
    private static String PROJECT_ID = CONFIG.getString("dialogflow.projectId");
    private static String LANGUAGE = CONFIG.getString("dialogflow.language");
    private static String TOKEN = CONFIG.getString("dialogflow.token");

    private DialogFlowHelper() { }

    public static SessionsClient createSessionClient() throws IOException {
        SessionsSettings sessionsSettings = SessionsSettings.newBuilder().setCredentialsProvider(() ->
                fromStream(new FileInputStream(resolvePath(TOKEN))))
                .build();
        return SessionsClient.create(sessionsSettings);
    }

    public static void consumeResponse(String sessionId, String text, Consumer<DetectIntentResponse> consumer) {
        try (SessionsClient sessionsClient = createSessionClient()) {
            DetectIntentResponse response = getDetectIntentResponse(sessionId, text, sessionsClient);
            consumer.accept(response);
        } catch (IOException e) {
            log.error("Failed to connect to DialogFlow", e);
        }
    }

    public static DetectIntentResponse getDetectIntentResponse(String sessionId, String text, SessionsClient sessionsClient) {
        SessionName session = SessionName.of(PROJECT_ID, sessionId);
        TextInput.Builder textInput = TextInput.newBuilder().setText(text).setLanguageCode(LANGUAGE);
        QueryInput queryInput = QueryInput.newBuilder().setText(textInput).build();
        DetectIntentResponse response =  sessionsClient.detectIntent(session, queryInput);
        log.debug("response: " + response.toString());
        return response;
    }

    public static <T> T mapResponse(String sessionId, String text, Function<DetectIntentResponse, T> mapper) {
        try (SessionsClient sessionsClient = createSessionClient()) {
            DetectIntentResponse response = getDetectIntentResponse(sessionId, text, sessionsClient);
            return mapper.apply(response);
        } catch (IOException e) {
            log.error("Failed to connect to DialogFlow", e);
        }
        return null;
    }
}
