package com.atlascan_spring.security.service;

import com.atlascan_spring.security.entities.User;
import com.atlascan_spring.security.entities.UserProfile;
import com.atlascan_spring.security.enums.AuthProvider;
import com.atlascan_spring.security.enums.Role;
import com.atlascan_spring.security.jwt.JwtTokenUtil;
import com.atlascan_spring.security.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final JwtTokenUtil jwtTokenUtil;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AuthService(UserRepository userRepository, JwtTokenUtil jwtTokenUtil,
                       AuthenticationManager authenticationManager, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.jwtTokenUtil = jwtTokenUtil;
        this.authenticationManager = authenticationManager;
        this.passwordEncoder = passwordEncoder;
    }
    public User registerUser(String email, String username, String password) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("User already exists with this email.");
        }

        User user = new User();
        user.setEmail(email);
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(Role.ROLE_USER);
        user.setAuthProvider(AuthProvider.LOCAL);

        UserProfile profile = new UserProfile();
        profile.setFirstName(username);
        profile.setLastName("");
        profile.setUser(user);

        user.setProfile(profile);

        return userRepository.save(user);
    }


    public Map<String, Object> login(String email, String password) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
        );

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));


        String token = jwtTokenUtil.generateToken(authentication);
        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("email", user.getEmail());
        response.put("username", user.getUsername());

        return response;
    }

}
