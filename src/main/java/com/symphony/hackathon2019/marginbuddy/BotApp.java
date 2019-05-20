package com.symphony.hackathon2019.marginbuddy;

import lombok.extern.slf4j.Slf4j;
import org.glassfish.jersey.apache.connector.ApacheConnectorProvider;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.symphonyoss.client.SymphonyClient;
import org.symphonyoss.client.SymphonyClientConfig;
import org.symphonyoss.client.SymphonyClientConfigID;
import org.symphonyoss.client.SymphonyClientFactory;
import org.symphonyoss.client.exceptions.AuthenticationException;
import org.symphonyoss.client.exceptions.InitException;
import org.symphonyoss.client.impl.CustomHttpClient;

import javax.ws.rs.client.Client;

import static org.symphonyoss.client.SymphonyClientConfigID.TRUSTSTORE_FILE;

@Slf4j
public class BotApp {
    public static void main(String[] args) {
        try {
            getSymphonyClient();
        } catch (Exception e) {
            log.error("Failed to connect to develop2.symphony.com", e);
        }
    }

    public static SymphonyClient getSymphonyClient()
            throws Exception {
        SymphonyClientConfig symphonyClientConfig = new SymphonyClientConfig(BotApp.class.getResource("/symphony.properties").getPath());
        symphonyClientConfig.set(TRUSTSTORE_FILE, BotApp.class.getResource("/certificates/all_symphony_certs_truststore").getPath());

        SymphonyClient symClient = SymphonyClientFactory.getClient(SymphonyClientFactory.TYPE.BASIC);
        String proxy = symphonyClientConfig.get("proxy.url");
        if (proxy == null) {
            symClient.init(symphonyClientConfig);
            return symClient;
        }

        ClientConfig clientConfig = new ClientConfig();
        clientConfig.connectorProvider(new ApacheConnectorProvider());
        clientConfig.property(ClientProperties.PROXY_URI, proxy);

        Client httpClient = CustomHttpClient.getClient(
                symphonyClientConfig.get(SymphonyClientConfigID.USER_CERT_FILE),
                symphonyClientConfig.get(SymphonyClientConfigID.USER_CERT_PASSWORD),
                symphonyClientConfig.get(TRUSTSTORE_FILE),
                symphonyClientConfig.get(SymphonyClientConfigID.TRUSTSTORE_PASSWORD),
                clientConfig);
        symClient.init(httpClient, symphonyClientConfig);
        return symClient;
    }
}
