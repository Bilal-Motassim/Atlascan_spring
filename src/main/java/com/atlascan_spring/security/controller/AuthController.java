package com.atlascan_spring.security.controller;

import com.atlascan_spring.security.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody Map<String, String> payload) {
        String email = payload.get("email");
        String username = payload.get("username");
        String password = payload.get("password");

        authService.registerUser(email, username, password);
        return ResponseEntity.ok("User registered successfully");
    }

    @PostMapping("/normal-login")
    public ResponseEntity<Map<String, Object>> loginUser(@RequestBody Map<String, String> payload) {
        String email = payload.get("email");
        String password = payload.get("password");

        Map<String, Object> response = authService.login(email, password);
        return ResponseEntity.ok(response);
    }
}
