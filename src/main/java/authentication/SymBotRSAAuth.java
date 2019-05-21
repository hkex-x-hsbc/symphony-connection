package authentication;

import clients.ISymClient;
import clients.symphony.api.APIClient;
import configuration.SymConfig;

import java.io.*;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status.Family;
import model.Token;
import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.apache.connector.ApacheConnectorProvider;
import org.glassfish.jersey.client.ClientConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.HttpClientBuilderHelper;
import utils.JwtHelper;

public class SymBotRSAAuth extends APIClient implements ISymAuth {
    private final Logger logger = LoggerFactory.getLogger(SymBotRSAAuth.class);
    private String sessionToken;
    private String kmToken;
    private SymConfig config;
    private Client sessionAuthClient;
    private Client kmAuthClient;
    private String jwt;
    private long lastAuthTime = 0L;
    private int authRetries = 0;

    public SymBotRSAAuth(SymConfig config) {
        this.config = config;
        ClientBuilder clientBuilder = HttpClientBuilderHelper.getHttpClientBuilderWithTruststore(config);
        Client client = clientBuilder.build();
        if (StringUtils.isEmpty(config.getProxyURL()) && StringUtils.isEmpty(config.getPodProxyURL())) {
            this.sessionAuthClient = client;
        } else {
            this.sessionAuthClient = clientBuilder.withConfig(HttpClientBuilderHelper.getClientConfig(config)).build();
        }

        if (StringUtils.isEmpty(config.getKeyManagerProxyURL())) {
            this.kmAuthClient = client;
        } else {
            ClientConfig clientConfig = new ClientConfig();
            clientConfig.connectorProvider(new ApacheConnectorProvider());
            clientConfig.property("jersey.config.client.proxy.uri", config.getKeyManagerProxyURL());
            if (config.getKeyManagerProxyUsername() != null && config.getKeyManagerProxyUsername() != null) {
                clientConfig.property("jersey.config.client.proxy.username", config.getKeyManagerProxyUsername());
                clientConfig.property("jersey.config.client.proxy.password", config.getKeyManagerProxyPassword());
            }

            this.kmAuthClient = clientBuilder.withConfig(clientConfig).build();
        }

    }

    public SymBotRSAAuth(SymConfig config, ClientConfig sessionAuthClientConfig, ClientConfig kmAuthClientConfig) {
        this.logger.info("SymOBOAuth with ClientConfig variables");
        this.config = config;
        ClientBuilder clientBuilder = HttpClientBuilderHelper.getHttpClientBuilderWithTruststore(config);
        if (sessionAuthClientConfig != null) {
            this.sessionAuthClient = clientBuilder.withConfig(sessionAuthClientConfig).build();
        } else {
            this.sessionAuthClient = clientBuilder.build();
        }

        if (kmAuthClientConfig != null) {
            this.kmAuthClient = clientBuilder.withConfig(kmAuthClientConfig).build();
        } else {
            this.kmAuthClient = clientBuilder.build();
        }

    }

    public void authenticate() {
        PrivateKey privateKey = null;

        try {
            InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(this.config.getBotPrivateKeyPath() + this.config.getBotPrivateKeyName());
            String jwtString = new BufferedReader(new InputStreamReader(inputStream)).lines().collect(Collectors.joining(System.lineSeparator()));
            privateKey = JwtHelper.parseRSAPrivateKey(jwtString);
        } catch (GeneralSecurityException var4) {
            this.logger.error("Error trying to parse RSA private key", var4);
        }

        if (this.lastAuthTime == 0L | System.currentTimeMillis() - this.lastAuthTime > 3000L) {
            this.logger.info("Last auth time was {}", this.lastAuthTime);
            this.logger.info("Now is {}", System.currentTimeMillis());
            this.jwt = JwtHelper.createSignedJwt(this.config.getBotUsername(), AuthEndpointConstants.JWT_EXPIRY_MS, privateKey);
            this.sessionAuthenticate();
            this.kmAuthenticate();
            this.lastAuthTime = System.currentTimeMillis();
        } else {
            try {
                this.logger.info("Re-authenticated too fast. Wait 30 seconds to try again.");
                TimeUnit.SECONDS.sleep(30L);
                this.authenticate();
            } catch (InterruptedException var3) {
                this.logger.error("Error with authentication", var3);
            }
        }

    }

    public void sessionAuthenticate() {
        Map<String, String> token = new HashMap();
        token.put("token", this.jwt);
        Response response = this.sessionAuthClient.target("https://" + this.config.getPodHost() + ":" + this.config.getPodPort()).path("/login/pubkey/authenticate").request(new String[]{"application/json"}).post(Entity.entity(token, "application/json"));
        if (response.getStatusInfo().getFamily() != Family.SUCCESSFUL) {
            try {
                this.handleError(response, (ISymClient)null);
            } catch (Exception var5) {
                this.logger.error("Unexpected error, retry authentication in 30 seconds");
            }

            try {
                TimeUnit.SECONDS.sleep(30L);
            } catch (InterruptedException var4) {
                this.logger.error("Error with authentication", var4);
            }

            if (this.authRetries++ > 5) {
                this.logger.error("Max retries reached. Giving up on auth.");
                return;
            }

            this.sessionAuthenticate();
        } else {
            this.sessionToken = ((Token)response.readEntity(Token.class)).getToken();
        }

    }

    public void kmAuthenticate() {
        Map<String, String> token = new HashMap();
        token.put("token", this.jwt);
        Response response = this.kmAuthClient.target("https://" + this.config.getKeyAuthHost() + ":" + this.config.getKeyAuthPort()).path("/relay/pubkey/authenticate").request(new String[]{"application/json"}).post(Entity.entity(token, "application/json"));
        if (response.getStatusInfo().getFamily() != Family.SUCCESSFUL) {
            try {
                this.handleError(response, (ISymClient)null);
            } catch (Exception var5) {
                this.logger.error("Unexpected error, retry authentication in 30 seconds");
            }

            try {
                TimeUnit.SECONDS.sleep(30L);
            } catch (InterruptedException var4) {
                this.logger.error("Error with authentication", var4);
            }

            if (this.authRetries++ > 5) {
                this.logger.error("Max retries reached. Giving up on auth.");
                return;
            }

            this.kmAuthenticate();
        } else {
            this.kmToken = ((Token)response.readEntity(Token.class)).getToken();
        }

    }

    public String getSessionToken() {
        return this.sessionToken;
    }

    public void setSessionToken(String sessionToken) {
        this.sessionToken = sessionToken;
    }

    public String getKmToken() {
        return this.kmToken;
    }

    public void setKmToken(String kmToken) {
        this.kmToken = kmToken;
    }

    public void logout() {
        this.logger.info("Logging out");
        Client client = ClientBuilder.newClient();
        Response response = client.target("https://" + this.config.getSessionAuthHost() + ":" + this.config.getSessionAuthPort()).path("/sessionauth/v1/logout").request(new String[]{"application/json"}).header("sessionToken", this.getSessionToken()).post((Entity)null);
        if (response.getStatusInfo().getFamily() != Family.SUCCESSFUL) {
            try {
                this.handleError(response, (ISymClient)null);
            } catch (Exception var4) {
                this.logger.error("Unexpected error, retry logout in 30 seconds", var4);
            }
        }

    }
}
