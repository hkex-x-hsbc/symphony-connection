package utils;

import configuration.SymConfig;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import javax.ws.rs.client.ClientBuilder;
import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.apache.connector.ApacheConnectorProvider;
import org.glassfish.jersey.client.ClientConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpClientBuilderHelper {
    private static final Logger logger = LoggerFactory.getLogger(HttpClientBuilderHelper.class);

    public HttpClientBuilderHelper() {
    }

    public static ClientBuilder getHttpClientBuilderWithTruststore(SymConfig config) {
        KeyStore jksStore = getJksKeystore();
        ClientBuilder clientBuilder = ClientBuilder.newBuilder();
        if (config.getTruststorePath() != null && jksStore != null) {
            loadTrustStore(config, jksStore, clientBuilder);
        }

        return clientBuilder;
    }

    public static ClientBuilder getHttpClientBotBuilder(SymConfig config) {
        KeyStore pkcsStore = getPkcsKeystore();
        KeyStore jksStore = getJksKeystore();

        try {
            InputStream keyStoreIS = loadInputStream(config.getBotCertPath() + config.getBotCertName());
            Throwable var4 = null;

            try {
                if (pkcsStore != null) {
                    pkcsStore.load(keyStoreIS, config.getBotCertPassword().toCharArray());
                }
            } catch (Throwable var14) {
                var4 = var14;
                throw var14;
            } finally {
                if (keyStoreIS != null) {
                    if (var4 != null) {
                        try {
                            keyStoreIS.close();
                        } catch (Throwable var13) {
                            var4.addSuppressed(var13);
                        }
                    } else {
                        keyStoreIS.close();
                    }
                }

            }
        } catch (NoSuchAlgorithmException | IOException | CertificateException var16) {
            logger.error("Error loading bot keystore file", var16);
        }

        ClientBuilder clientBuilder = ClientBuilder.newBuilder().keyStore(pkcsStore, config.getBotCertPassword().toCharArray());
        if (config.getTruststorePath() != null && jksStore != null) {
            loadTrustStore(config, jksStore, clientBuilder);
        }

        return clientBuilder;
    }

    public static ClientBuilder getHttpClientAppBuilder(SymConfig config) {
        KeyStore pkcsStore = getPkcsKeystore();
        KeyStore jksStore = getJksKeystore();

        try {
            InputStream keyStoreIS = loadInputStream(config.getAppCertPath() + config.getAppCertName());
            Throwable var4 = null;

            try {
                if (pkcsStore != null) {
                    pkcsStore.load(keyStoreIS, config.getBotCertPassword().toCharArray());
                }
            } catch (Throwable var14) {
                var4 = var14;
                throw var14;
            } finally {
                if (keyStoreIS != null) {
                    if (var4 != null) {
                        try {
                            keyStoreIS.close();
                        } catch (Throwable var13) {
                            var4.addSuppressed(var13);
                        }
                    } else {
                        keyStoreIS.close();
                    }
                }

            }
        } catch (NoSuchAlgorithmException | IOException | CertificateException var16) {
            logger.error("Error loading app keystore file", var16);
        }

        ClientBuilder clientBuilder = ClientBuilder.newBuilder().keyStore(pkcsStore, config.getAppCertPassword().toCharArray());
        if (config.getTruststorePath() != null && jksStore != null) {
            loadTrustStore(config, jksStore, clientBuilder);
        }

        return clientBuilder;
    }

    public static ClientConfig getClientConfig(SymConfig config) {
        String proxyURL = !StringUtils.isEmpty(config.getPodProxyURL()) ? config.getPodProxyURL() : config.getProxyURL();
        String proxyUser = !StringUtils.isEmpty(config.getPodProxyUsername()) ? config.getPodProxyUsername() : config.getProxyUsername();
        String proxyPass = !StringUtils.isEmpty(config.getPodProxyPassword()) ? config.getPodProxyPassword() : config.getProxyPassword();
        ClientConfig clientConfig = new ClientConfig();
        clientConfig.connectorProvider(new ApacheConnectorProvider());
        if (!StringUtils.isEmpty(proxyURL)) {
            clientConfig.property("jersey.config.client.proxy.uri", proxyURL);
            if (!StringUtils.isEmpty(proxyUser) && !StringUtils.isEmpty(proxyPass)) {
                clientConfig.property("jersey.config.client.proxy.username", proxyUser);
                clientConfig.property("jersey.config.client.proxy.password", proxyPass);
            }
        }

        return clientConfig;
    }

    private static void loadTrustStore(SymConfig config, KeyStore tks, ClientBuilder clientBuilder) {
        try {
            InputStream trustStoreIS = loadInputStream(config.getTruststorePath());
            Throwable var4 = null;

            try {
                tks.load(trustStoreIS, config.getTruststorePassword().toCharArray());
                clientBuilder.trustStore(tks);
            } catch (Throwable var14) {
                var4 = var14;
                throw var14;
            } finally {
                if (trustStoreIS != null) {
                    if (var4 != null) {
                        try {
                            trustStoreIS.close();
                        } catch (Throwable var13) {
                            var4.addSuppressed(var13);
                        }
                    } else {
                        trustStoreIS.close();
                    }
                }

            }
        } catch (NoSuchAlgorithmException | IOException | CertificateException var16) {
            logger.error("Error loading truststore", var16);
        }

    }

    private static InputStream loadInputStream(String fileName) throws FileNotFoundException {
        if ((new File(fileName)).exists()) {
            return new FileInputStream(fileName);
        } else if (HttpClientBuilderHelper.class.getClassLoader().getResource(fileName) != null) {
            return HttpClientBuilderHelper.class.getClassLoader().getResourceAsStream(fileName);
        } else {
            throw new FileNotFoundException();
        }
    }

    private static KeyStore getPkcsKeystore() {
        try {
            return KeyStore.getInstance("PKCS12");
        } catch (KeyStoreException var1) {
            logger.error("Error creating PKCS keystore instance", var1);
            return null;
        }
    }

    private static KeyStore getJksKeystore() {
        try {
            return KeyStore.getInstance("JKS");
        } catch (KeyStoreException var1) {
            logger.error("Error creating JKS keystore instance", var1);
            return null;
        }
    }
}
