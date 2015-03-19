package com.zanox.api.advertiser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import org.apache.commons.codec.binary.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/*
* Client for zanox REST Advertiser API that authorizes with query parameters.
* */

public class AdvertiserClient {

    private final static String APP_URL = "https://advertiser.api.zanox.com/advertiser-api/2015-03-01";
    private final static String BASE_REST_APP = "/report/program/";
    private final static int ARGUMENTS_NUMBER = 5;
    private static String params = "?groupby={0}&fromdate={1}&todate={2}";
    private static String auth = "&connectid={0}&date={1}&nonce={2}&signature={3}";

    public static void main(String[] args) throws GeneralSecurityException, UnsupportedEncodingException {

        if (args.length != ARGUMENTS_NUMBER) {
            System.err.println("Wrong number of arguments. Correct usage: java -jar advertiser-api-client-1.0-SNAPSHOT.jar --[header|url] PROGRAM_ID CONNECT_ID SECRET_KEY GROUP_BY");
            System.exit(1);
        }

        String authType = args[0];
        String programId = args[1];
        String connectId = args[2];
        String secretKey = args[3];
        String groupBy = args[4];

        if (!isGroupByValid(groupBy)) {
            groupBy = "day";
            System.out.println("Group by parameter invalid, default group by: day will be used.");
        }

        Client client = ClientBuilder.newBuilder().register(JacksonJsonProvider.class).build();
        Response response = null;
        params = fillTheParams(groupBy);

        if (authType.equalsIgnoreCase("--header")) {
            WebTarget target = client.target(APP_URL + BASE_REST_APP + programId + params);
            response = target.request(MediaType.APPLICATION_JSON_TYPE).headers(fillTheAuthorizationHeaders(programId, connectId, secretKey)).get();

        } else if (authType.equalsIgnoreCase("--url")) {
            auth = fillTheAuthorization(programId, connectId, secretKey);
            WebTarget target = client.target(APP_URL + BASE_REST_APP + programId + params + auth);
            response = target.request(MediaType.APPLICATION_JSON_TYPE).get();
        }

        System.out.println(response.getStatus());
        JsonNode json = response.readEntity(JsonNode.class);
        System.out.println(json);
    }

    private static boolean isGroupByValid(String groupBy) {
        List<String> validGroupByList = Arrays.asList("day", "month", "adspace", "admedium");
        return validGroupByList.contains(groupBy);
    }

    /**
     * Fills the authentication information into url parameters
     *
     * @param connectId
     * @param secretKey
     * @return the String with filled in authentication information
     * @throws GeneralSecurityException
     */

    private static String fillTheAuthorization(String programId, String connectId, String secretKey) throws GeneralSecurityException, UnsupportedEncodingException {
        String restTs = getRestTimestamp();
        String restNonce = generateNonce();
        String restSignature = URLEncoder.encode(new String(getRestSignature("GET", BASE_REST_APP + programId, restTs, restNonce, secretKey)), "UTF-8");
        return MessageFormat.format(auth, connectId, restTs, restNonce, restSignature);
    }

    /**
     * Fills the authentication information into header parameters
     *
     * @param programId
     * @param connectId
     * @param secretKey
     * @return
     * @throws GeneralSecurityException
     */

    private static MultivaluedMap<String, Object> fillTheAuthorizationHeaders(String programId, String connectId, String secretKey) throws GeneralSecurityException, UnsupportedEncodingException {
        MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();
        String restTs = getRestTimestamp();
        String restNonce = generateNonce();
        String restSignature = new String(getRestSignature("GET", BASE_REST_APP + programId, restTs, restNonce, secretKey));

        headers.add("Authorization", "ZXWS" + " " + connectId + ":" + restSignature);
        headers.add("Date", restTs);
        headers.add("nonce", restNonce);

        return headers;
    }

    /**
     * Fills in the parameters. The requested time range is set to 1 month
     *
     * @param groupBy
     * @return the String containing required parameters
     */
    private static String fillTheParams(String groupBy) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date now = new Date();
        String toDate = sdf.format(addDays(now, -1));
        String fromDate = sdf.format(addDays(now, -31));
        return MessageFormat.format(params, groupBy, fromDate, toDate);
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
    private static byte[] getRestSignature(String httpVerb, String uri, String timestamp, String nonce, String secretKey) throws GeneralSecurityException, UnsupportedEncodingException {
        String stringToSign = httpVerb + uri.toLowerCase() + timestamp + nonce;
        return getSignature(stringToSign, secretKey);
    }


    private static byte[] getSignature(String stringToSign, String secretKey) throws GeneralSecurityException, UnsupportedEncodingException {
        SecretKeySpec signingKey = new SecretKeySpec(secretKey.getBytes(), "HmacSHA1");
        Mac mac = Mac.getInstance("HmacSHA1");
        mac.init(signingKey);

        byte[] rawHmac = mac.doFinal(stringToSign.getBytes());
        return Base64.encodeBase64(rawHmac);
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
