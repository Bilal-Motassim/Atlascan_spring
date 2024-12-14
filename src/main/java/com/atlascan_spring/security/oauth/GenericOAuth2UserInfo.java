package com.atlascan_spring.security.oauth;

import java.util.Map;

public class GenericOAuth2UserInfo extends OAuth2UserInfo {

    public GenericOAuth2UserInfo(Map<String, Object> attributes) {
        super(attributes);
    }

    @Override
    public String getEmail() {
        if (attributes.containsKey("email")) {
            return (String) attributes.get("email");
        } else if (attributes.containsKey("user_email")) {
            return (String) attributes.get("user_email");
        }
        return null;
    }

    @Override
    public String getName() {
        if (attributes.containsKey("name")) {
            return (String) attributes.get("name");
        } else if (attributes.containsKey("login")) {
            return (String) attributes.get("login");
        }
        return null;
    }
}
