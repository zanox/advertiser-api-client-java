package com.zanox.api.advertiser;

import javax.ws.rs.core.MultivaluedMap;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;

/**
 * Created by michael.renz on 09.09.2015.
 */
public class PartnerCodeService implements RestService {

	private final static String BASE_REST_URL = "/member/partnership";
	private final static int ARGUMENTS_NUMBER = 5;
	private String connectId;
	private String secretKey;
	private String partnerCodes;

	public PartnerCodeService(String... args) {
		if (args.length != ARGUMENTS_NUMBER) {
			System.err.println("Wrong number of arguments. Correct usage: java -jar advertiser-api-client-1.0-SNAPSHOT.jar --[header|url] SERVICETYPE CONNECT_ID SECRET_KEY COMMA_SEPARATED_PARTNER_CODES");
			System.exit(1);
		}

		this.connectId = args[2];
		this.secretKey = args[3];
		this.partnerCodes = args[4];
	}

	@Override public String getBaseRestUrl() {
		return BASE_REST_URL + getPartnerCodeParams();
	}

	@Override
	public String getAuthorizationParams() throws GeneralSecurityException, UnsupportedEncodingException {
		return AuthenticationUtil.createAuthorizationUrlParams(BASE_REST_URL, this.connectId, this.secretKey);
	}

	@Override
	public MultivaluedMap<String, Object> getAuthorizationHeaders() throws GeneralSecurityException, UnsupportedEncodingException {
		return AuthenticationUtil.createAuthorizationHeaders(BASE_REST_URL, this.connectId, this.secretKey);
	}

	public String getPartnerCodeParams() {
		return "?partnerCode=" + partnerCodes;
	}
}
