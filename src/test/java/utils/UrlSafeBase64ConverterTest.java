package utils;

import org.junit.Assert;
import org.junit.Test;

public class UrlSafeBase64ConverterTest {

    @Test
    public void converterTest() {
        String standardString = "hLlkBpxkld6FQeBzQSaWx3///pUhP/MRdA==";
        String encodedString = UrlSafeBase64Converter.encode(standardString);
        System.out.println(encodedString);
        Assert.assertEquals(standardString, UrlSafeBase64Converter.decode(encodedString));
    }
}
