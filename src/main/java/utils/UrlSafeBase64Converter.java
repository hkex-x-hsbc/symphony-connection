package utils;

import org.apache.commons.lang3.StringUtils;

public class UrlSafeBase64Converter {

    private static final char SLASH = '/';
    private static final char UNDERSCORE = '_';
    private static final char PLUS = '+';
    private static final char MINUS = '-';
    private static final String EQUAL = "=";
    private static final int SYMPHONY_IDENTIFIER_LENGTH = 36;

    public static String encode(String standardBase64String) {
        String urlSafeBase64String = standardBase64String.replace(SLASH, UNDERSCORE).replace(PLUS, MINUS);
        while (urlSafeBase64String.endsWith(EQUAL)) {
            urlSafeBase64String = StringUtils.chop(urlSafeBase64String);
        }
        return urlSafeBase64String;
    }

    public static String decode(String urlSafeBase64String) {
        String standardBase64String = urlSafeBase64String.replace(UNDERSCORE, SLASH).replace(MINUS, PLUS);
        while (standardBase64String.length() < SYMPHONY_IDENTIFIER_LENGTH) {
            standardBase64String = standardBase64String.concat(EQUAL);
        }
        return standardBase64String;
    }
}
