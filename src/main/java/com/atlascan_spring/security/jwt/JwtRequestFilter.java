package com.atlascan_spring.security.jwt;

import com.atlascan_spring.security.service.CustomUserDetailsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.ServletException;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtRequestFilter.class);
    private final JwtTokenUtil jwtTokenUtil;
    private final CustomUserDetailsService userDetailsService;

    @Autowired
    public JwtRequestFilter(JwtTokenUtil jwtTokenUtil, CustomUserDetailsService userDetailsService) {
        this.jwtTokenUtil = jwtTokenUtil;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        try {
            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                String jwtToken = extractJwtFromRequest(request);
                if (jwtToken != null && jwtTokenUtil.validateToken(jwtToken)) {
                    logger.debug("Valid JWT found: {}", jwtToken);
                    setupSecurityContext(jwtToken, request);
                } else {
                    logger.debug("Invalid or missing JWT");
                }
            } else {
                logger.debug("SecurityContext already populated, skipping JWT filter.");
            }
        } catch (Exception e) {
            logger.error("Error in JWT filter: ", e);
        }
        chain.doFilter(request, response);
    }

    private void setupSecurityContext(String jwtToken, HttpServletRequest request) {
        String username = jwtTokenUtil.getUsernameFromToken(jwtToken);
        List<GrantedAuthority> authorities = jwtTokenUtil.getRolesFromToken(jwtToken).stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
        String email = jwtTokenUtil.getEmailFromToken(jwtToken);
        String authProvider = jwtTokenUtil.getAuthProviderFromToken(jwtToken);

        if (username == null || email == null || authProvider == null) {
            logger.warn("JWT is missing required claims");
            return;
        }

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                username, null, authorities);
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        logger.debug("SecurityContext set with user: {}, email: {}, provider: {}", username, email, authProvider);
    }



    private String extractJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        logger.warn("No JWT found in request headers or the token does not start with 'Bearer '");
        return null;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        return path.startsWith("/oauth2") || path.startsWith("/login/oauth2") || path.startsWith("/swagger");
    }

}
