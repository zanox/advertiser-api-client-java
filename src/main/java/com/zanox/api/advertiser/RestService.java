package com.zanox.api.advertiser;

import javax.ws.rs.core.MultivaluedMap;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;

/**
 * Created by michael.renz on 09.09.2015.
 */
public interface RestService {

	String getBaseRestUrl();

	String getAuthorizationParams() throws GeneralSecurityException, UnsupportedEncodingException;

	MultivaluedMap<String, Object> getAuthorizationHeaders() throws GeneralSecurityException, UnsupportedEncodingException;
}
