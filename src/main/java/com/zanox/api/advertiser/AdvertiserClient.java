package com.zanox.api.advertiser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;

/*
* Client for zanox REST Advertiser API that authorizes with query parameters.
* */

public class AdvertiserClient {

    private static final String APP_URL = "https://advertiser.api.zanox.com/advertiser-api/2015-03-01";
    private static final int STATUS_OK = 200;

    public static void main(String... args)  {
        String authType = args[0];
        String serviceType = args[1];

        RestService restService;
        switch(serviceType) {
            case "reportservice" :
                restService = new ReportService(args);
                break;
            case "partnercodeservice" :
                restService = new PartnerCodeService(args);
                break;
            default:
                throw new IllegalArgumentException("Wrong serviceType set, valid values are [reportservice, partnercodeservice]");
        }

        Response response = null;
        try {
            response = queryRestService(authType, restService);
        } catch (GeneralSecurityException | UnsupportedEncodingException ex) {
            ex.printStackTrace();
        }

        System.out.println("Response status:" + response.getStatus());
        if(response.getStatus() == STATUS_OK) {
            JsonNode json = response.readEntity(JsonNode.class);
            System.out.println("Response:" + json);
        }
    }


    private static Response queryRestService(String authType, RestService restService) throws GeneralSecurityException, UnsupportedEncodingException {
        Client client = ClientBuilder.newBuilder().register(JacksonJsonProvider.class).build();

        if (authType.equalsIgnoreCase("--header")) {
            return requestByHeader(client, restService.getBaseRestUrl(), restService.getAuthorizationHeaders());
        } else if (authType.equalsIgnoreCase("--url")) {
            return requestByUrl(client, restService.getBaseRestUrl(), restService.getAuthorizationParams());
        } else{
            throw new IllegalArgumentException("wrong authtype param, allowed values are --[header|url]");
        }
    }

    private static Response requestByUrl(Client client, String restUrl, String authenticationParams) {
        WebTarget target = client.target(APP_URL + restUrl + authenticationParams);
        return target.request(MediaType.APPLICATION_JSON_TYPE).get();
    }

    private static Response requestByHeader(Client client, String restUrl, MultivaluedMap<String, Object> headers) {
        WebTarget target = client.target(APP_URL + restUrl);
        return target.request(MediaType.APPLICATION_JSON_TYPE).headers(headers).get();
    }
}
