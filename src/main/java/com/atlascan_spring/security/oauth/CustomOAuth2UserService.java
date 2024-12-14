package com.atlascan_spring.security.oauth;

import com.atlascan_spring.security.entities.User;
import com.atlascan_spring.security.entities.UserProfile;
import com.atlascan_spring.security.enums.AuthProvider;
import com.atlascan_spring.security.enums.Role;
import com.atlascan_spring.security.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Autowired
    public CustomOAuth2UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        OAuth2UserInfo oAuth2UserInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(registrationId, oAuth2User.getAttributes());
        if (oAuth2UserInfo.getEmail() == null) {
            throw new OAuth2AuthenticationException("Email not found from OAuth2 provider");
        }

        User user = processOAuth2User(oAuth2UserInfo);

        return new CustomOAuth2User(oAuth2User, user);
    }

    private User processOAuth2User(OAuth2UserInfo oAuth2UserInfo) {
        Optional<User> userOptional = userRepository.findByEmail(oAuth2UserInfo.getEmail());
        User user;

        if (userOptional.isPresent()) {
            user = userOptional.get();
            user.setUsername(oAuth2UserInfo.getName());
            user.setAuthProvider(AuthProvider.GOOGLE);
        } else {
            user = new User();
            user.setEmail(oAuth2UserInfo.getEmail());
            user.setUsername(oAuth2UserInfo.getName());
            user.setPassword(null);
            user.setAuthProvider(AuthProvider.GOOGLE);
            user.setRole(Role.ROLE_USER);

            UserProfile profile = new UserProfile();
            profile.setFirstName(oAuth2UserInfo.getName());
            profile.setLastName("");
            profile.setUser(user);

            user.setProfile(profile);
        }
        return userRepository.save(user);
    }



    private User registerNewUser(OAuth2UserInfo oAuth2UserInfo) {
        User user = new User();
        user.setEmail(oAuth2UserInfo.getEmail());
        user.setUsername(oAuth2UserInfo.getName());
        user.setPassword(null);
        user.setRole(Role.ROLE_USER);
        user.setAuthProvider(AuthProvider.GOOGLE);
        UserProfile profile = new UserProfile();
        profile.setFirstName(oAuth2UserInfo.getName());
        profile.setLastName("");
        profile.setUser(user);

        user.setProfile(profile);

        return user;
    }

}

