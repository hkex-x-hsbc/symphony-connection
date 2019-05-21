package utils;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.io.CharArrayReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.Date;
import org.apache.commons.io.FileUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.slf4j.LoggerFactory;

public class JwtHelper {
    private static final String PEM_PRIVATE_START = "-----BEGIN PRIVATE KEY-----";
    private static final String PEM_PRIVATE_END = "-----END PRIVATE KEY-----";
    private static final String PEM_RSA_PRIVATE_START = "-----BEGIN RSA PRIVATE KEY-----";
    private static final String PEM_RSA_PRIVATE_END = "-----END RSA PRIVATE KEY-----";

    public JwtHelper() {
    }

    public static String createSignedJwt(String user, long expiration, Key privateKey) {
        return Jwts.builder().setSubject(user).setExpiration(new Date(System.currentTimeMillis() + expiration)).signWith(SignatureAlgorithm.RS512, privateKey).compact();
    }

    public static PrivateKey parseRSAPrivateKey(File pemPrivateKeyFile) throws IOException, GeneralSecurityException {
        return parseRSAPrivateKey(FileUtils.readFileToString(pemPrivateKeyFile, Charset.defaultCharset()));
    }

    public static PrivateKey parseRSAPrivateKey(String pemPrivateKey) throws GeneralSecurityException {
        if (!pemPrivateKey.contains("-----BEGIN PRIVATE KEY-----") && !pemPrivateKey.contains("-----BEGIN RSA PRIVATE KEY-----")) {
            throw new GeneralSecurityException("Invalid private key.");
        } else {
            String privKeyPEM = pemPrivateKey.replace("-----BEGIN PRIVATE KEY-----", "").replace("-----END PRIVATE KEY-----", "").replace("-----BEGIN RSA PRIVATE KEY-----", "").replace("-----END RSA PRIVATE KEY-----", "").replaceAll("\\n", "\n").replaceAll("\\s", "");
            if (pemPrivateKey.contains("-----BEGIN PRIVATE KEY-----")) {
                try {
                    PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(privKeyPEM));
                    KeyFactory kf = KeyFactory.getInstance("RSA");
                    return kf.generatePrivate(spec);
                } catch (InvalidKeySpecException var5) {
                    throw new GeneralSecurityException("Invalid PKCS#8 private key.");
                }
            } else {
                Security.addProvider(new BouncyCastleProvider());
                PEMParser pemParser = new PEMParser(new CharArrayReader(pemPrivateKey.toCharArray()));
                JcaPEMKeyConverter converter = (new JcaPEMKeyConverter()).setProvider("BC");

                try {
                    KeyPair kp = converter.getKeyPair((PEMKeyPair)pemParser.readObject());
                    return kp.getPrivate();
                } catch (IOException var6) {
                    throw new GeneralSecurityException("Invalid PKCS#1 private key.");
                }
            }
        }
    }

    public static Object validateJwt(String jwt, String certificate) {
        try {
            X509Certificate x509Certificate = CertificateUtils.parseX509Certificate(certificate);
            PublicKey publicKey = x509Certificate.getPublicKey();
            JwtConsumer jwtConsumer = (new JwtConsumerBuilder()).setVerificationKey(publicKey).setSkipAllValidators().build();
            JwtClaims jwtDecoded = jwtConsumer.processToClaims(jwt);
            return jwtDecoded.getClaimValue("user");
        } catch (InvalidJwtException | GeneralSecurityException var6) {
            LoggerFactory.getLogger(JwtHelper.class).error("Error with decoding jwt", var6);
            return null;
        }
    }
}
