package com.atlascan_spring.security.oauth;

import java.util.Map;

public class OAuth2UserInfoFactory {

    public static OAuth2UserInfo getOAuth2UserInfo(String registrationId, Map<String, Object> attributes) {
        if (registrationId.equalsIgnoreCase("google") || registrationId.equalsIgnoreCase("github")) {
            return new GenericOAuth2UserInfo(attributes);
        }
        throw new IllegalArgumentException("Login with " + registrationId + " is not supported yet.");
    }
}
