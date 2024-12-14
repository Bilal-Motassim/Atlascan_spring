package com.atlascan_spring.security.jwt;

import com.atlascan_spring.security.oauth.CustomOAuth2User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.*;
import java.util.stream.Collectors;


@Component
public class JwtTokenUtil {

    private final Key key;
    private static final Logger log = LoggerFactory.getLogger(JwtTokenUtil.class);

    @Value("${jwt.expirationMs}")
    private int jwtExpirationMs;

    public JwtTokenUtil(@Value("${jwt.secret}") String secret) {
        this.key = new SecretKeySpec(secret.getBytes(), SignatureAlgorithm.HS512.getJcaName());
    }


    public String generateToken(Authentication authentication) {
        String username = authentication.getName();
        String email = null;
        List<String> roles;

        if (authentication instanceof OAuth2AuthenticationToken) {
            OAuth2AuthenticationToken oauth2Token = (OAuth2AuthenticationToken) authentication;
            email = oauth2Token.getPrincipal().getAttribute("email");
            roles = Collections.singletonList("ROLE_USER");
        } else if (authentication instanceof UsernamePasswordAuthenticationToken) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof CustomOAuth2User) {
                email = ((CustomOAuth2User) principal).getUser().getEmail();
                roles = Collections.singletonList(((CustomOAuth2User) principal).getUser().getRole().name());
            } else {
                roles = authentication.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toList());
            }
        } else {
            throw new IllegalArgumentException("Unsupported authentication type");
        }

        Claims claims = Jwts.claims().setSubject(username);
        claims.put("email", email);
        claims.put("roles", roles);
        claims.put("authProvider", authentication instanceof OAuth2AuthenticationToken ? "google" : "local");

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key)
                .compact();
    }

    public List<String> getRolesFromToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return claims.get("roles", List.class);
        } catch (Exception e) {
            log.error("Failed to extract roles from JWT", e);
            return List.of();
        }
    }

    public String getUsernameFromToken(String token) {
        try {
            String username = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();
            log.debug("Nom d'utilisateur extrait du JWT : {}", username);
            return username;
        } catch (Exception e) {
            log.error("Erreur lors de l'extraction du nom d'utilisateur depuis le JWT", e);
            throw e;
        }
    }


    public boolean validateToken(String authToken) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(authToken);
            return true;
        } catch (Exception e) {
            log.error("Token validation error: {}", e.getMessage());
            return false;
        }
    }

    public String getAuthProviderFromToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return claims.get("authProvider", String.class);
        } catch (Exception e) {
            log.error("Failed to extract authProvider from JWT", e);
            return null;
        }
    }

    public String getEmailFromToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            return claims.get("email", String.class);
        } catch (Exception e) {
            log.error("Failed to extract email from JWT", e);
            return null;
        }
    }


}
