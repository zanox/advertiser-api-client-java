package com.zanox.api.advertiser;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import org.apache.commons.codec.binary.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;
import java.util.TimeZone;
import java.util.Calendar;

public class AdvertiserClient {

    private final static String APP_URL = "http://advertiser.api.zanox.com/advertiser-api/report";
    private final static String BASE_REST_APP = "/2015-03-01/program/";
    private final static String PROGRAM_ID = "1803";
    private static String params = "?groupby=adspace&fromdate={0}&todate={1}";
    private static String auth = "&connectid={0}&date={1}&nonce={2}&signature={3}";

    public static void main(String[] args) throws GeneralSecurityException {

        if (args.length != 2) {
            System.err.println("Usage: java -jar advertiser-api-client-1.0-SNAPSHOT.jar CONNECT_ID SECRET_KEY");
            System.exit(1);
        }

        String connectId = args[0];
        String secretKey = args[1];

        Client client = ClientBuilder.newBuilder().register(JacksonJsonProvider.class).build();

        params = fillTheParams();
        auth = fillTheAuthorization(connectId, secretKey);

        WebTarget target = client.target(APP_URL + BASE_REST_APP + PROGRAM_ID + params + auth);

        Response response = target.request(MediaType.APPLICATION_JSON_TYPE).get();

        System.out.println(response.getStatus());
        JsonNode json = response.readEntity(JsonNode.class);
        System.out.println(json);

    }

    private static String fillTheAuthorization(String connectId, String secretKey) throws GeneralSecurityException {
        String restTs = getRestTimestamp();
        String restNonce = generateNonce();
        String restSignature = getRestSignature("GET", BASE_REST_APP + PROGRAM_ID, restTs, restNonce, secretKey);
        return MessageFormat.format(auth, connectId, restTs, restNonce, restSignature);
    }

    private static String fillTheParams() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date now = new Date();
        String toDate = sdf.format(addDays(now, -1));
        String fromDate = sdf.format(addDays(now, -31));
        return MessageFormat.format(params, fromDate, toDate);
    }


    /**
     * Generates a nonce for REST requests
     *
     * @return nonce
     * @throws java.security.NoSuchAlgorithmException
     */
    private static String generateNonce() throws NoSuchAlgorithmException {
        long currentTime = System.currentTimeMillis();
        long randomNumber = Math.abs(new Random().nextInt());

        String msg = Long.toString(currentTime) + Long.toString(randomNumber);
        MessageDigest algorithm = MessageDigest.getInstance("MD5");
        return hex(algorithm.digest(msg.getBytes()));

    }

    /**
     * Generates a timestamp string for REST API authentication
     *
     * @return timestamp
     */
    private static String getRestTimestamp() {
        return getTimestamp("EEE, dd MMM yyyy HH:mm:ss") + " GMT";
    }

    /**
     * Generates a signature for a REST API request
     *
     * @param httpVerb  GET, POST, PUT or DELETE
     * @param uri       resource URI that follows API version date 2011-03-01. e.g. for http://api.zanox.com/xml/2011-03-01/adspaces URL, use /adspaces as
     *                  URI
     * @param timestamp timestamp string for REST authentication
     * @param nonce     nonce
     * @param secretKey your secret key
     * @return REST signature
     * @throws java.security.GeneralSecurityException
     */
    private static String getRestSignature(String httpVerb, String uri, String timestamp, String nonce, String secretKey) throws GeneralSecurityException {
        String stringToSign = httpVerb + uri.toLowerCase() + timestamp + nonce;
        return getSignature(stringToSign, secretKey);
    }


    private static String getSignature(String stringToSign, String secretKey) throws GeneralSecurityException {
        SecretKeySpec signingKey = new SecretKeySpec(secretKey.getBytes(), "HmacSHA1");
        Mac mac = Mac.getInstance("HmacSHA1");
        mac.init(signingKey);

        byte[] rawHmac = mac.doFinal(stringToSign.getBytes());
        byte[] encoded = Base64.encodeBase64(rawHmac);
        return new String(encoded);

    }

    private static String hex(byte[] array) {
        StringBuilder buffer = new StringBuilder();


        for (byte anArray : array) {
            buffer.append(Integer.toHexString(anArray & 0xFF | 0x100).toUpperCase().substring(1, 3));
        }

        return buffer.toString();
    }

    private static String getTimestamp(String dateFormat) {
        SimpleDateFormat df = new SimpleDateFormat(dateFormat, Locale.US);
        df.setTimeZone(TimeZone.getTimeZone("GMT"));
        return df.format(new Date());
    }

    public static Date addDays(Date baseDate, int daysToAdd) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(baseDate);
        calendar.add(Calendar.DAY_OF_YEAR, daysToAdd);
        return calendar.getTime();
    }

}
