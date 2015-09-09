package com.zanox.api.advertiser;

import org.apache.commons.codec.binary.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;
import java.util.TimeZone;

public class AuthenticationUtil {

	private static String AUTHENTICATION_PARAMS = "&connectid={0}&date={1}&nonce={2}&signature={3}";

	private AuthenticationUtil() {}

	/**
	 * Create the authentication information as url parameters
	 *
	 * @param restUrl
	 * @param connectId
	 * @param secretKey
	 * @return the String with filled in authentication information
	 * @throws GeneralSecurityException
	 */

	public static String createAuthorizationUrlParams(String restUrl, String connectId, String secretKey) throws GeneralSecurityException, UnsupportedEncodingException {
		String restTimestamp = getRestTimestamp();
		String restNonce = generateNonce();
		String restSignature = URLEncoder.encode(getRestSignature("GET", restUrl, restTimestamp, restNonce,
				secretKey),
			"UTF-8");
		return MessageFormat.format(AUTHENTICATION_PARAMS, connectId, restTimestamp, restNonce, restSignature);
	}

	/**
	 * Create the authentication information as header parameters
	 *
	 * @param restUrl
	 * @param connectId
	 * @param secretKey
	 * @return
	 * @throws GeneralSecurityException
	 */
	public static MultivaluedMap<String, Object> createAuthorizationHeaders(String restUrl, String connectId, String secretKey) throws GeneralSecurityException, UnsupportedEncodingException {
		MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();
		String restTimestamp = getRestTimestamp();
		String restNonce = generateNonce();
		String restSignature = getRestSignature("GET", restUrl, restTimestamp, restNonce, secretKey);

		headers.add("Authorization", "ZXWS" + " " + connectId + ":" + restSignature);
		headers.add("Date", restTimestamp);
		headers.add("nonce", restNonce);

		return headers;
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
	 * @param uri       resource URI that follows API version 2015-03-01: https://advertiser.api.zanox.com/advertiser-api/2015-03-01 URL, use /report/program/ as URI
	 * @param timestamp timestamp string for REST authentication
	 * @param nonce     nonce
	 * @param secretKey your secret key
	 * @return REST signature
	 * @throws java.security.GeneralSecurityException
	 */
	private static String getRestSignature(String httpVerb, String uri, String timestamp, String nonce, String secretKey) throws
		GeneralSecurityException, UnsupportedEncodingException {
		String stringToSign = httpVerb + uri.toLowerCase() + timestamp + nonce;
		return getSignature(stringToSign, secretKey);
	}


	private static String getSignature(String stringToSign, String secretKey) throws GeneralSecurityException, UnsupportedEncodingException {
		SecretKeySpec signingKey = new SecretKeySpec(secretKey.getBytes(), "HmacSHA1");
		Mac mac = Mac.getInstance("HmacSHA1");
		mac.init(signingKey);

		byte[] rawHmac = mac.doFinal(stringToSign.getBytes());
		return Base64.encodeBase64String(rawHmac);
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
}
