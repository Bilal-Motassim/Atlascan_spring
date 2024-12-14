package com.atlascan_spring.security.config;

import com.atlascan_spring.security.entities.User;
import com.atlascan_spring.security.entities.UserProfile;
import com.atlascan_spring.security.enums.AuthProvider;
import com.atlascan_spring.security.enums.Role;
import com.atlascan_spring.security.jwt.JwtRequestFilter;
import com.atlascan_spring.security.jwt.JwtTokenUtil;
import com.atlascan_spring.security.oauth.CustomOAuth2User;
import com.atlascan_spring.security.oauth.CustomOAuth2UserService;
import com.atlascan_spring.security.repository.UserRepository;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtRequestFilter jwtRequestFilter;
    private final AuthenticationConfiguration authenticationConfiguration;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final JwtTokenUtil jwtTokenUtil;

    private final UserRepository userRepository;


    public SecurityConfig(JwtRequestFilter jwtRequestFilter,
                          AuthenticationConfiguration authenticationConfiguration,
                          CustomOAuth2UserService customOAuth2UserService,
                          JwtTokenUtil jwtTokenUtil, UserRepository userRepository) {
        this.jwtRequestFilter = jwtRequestFilter;
        this.authenticationConfiguration = authenticationConfiguration;
        this.customOAuth2UserService = customOAuth2UserService;
        this.jwtTokenUtil = jwtTokenUtil;
        this.userRepository = userRepository;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/auth/**",
                                "/oauth2/**",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/login/**"


                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
                        })
                )
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/login")
                        .successHandler((request, response, authentication) -> {
                            Object principal = authentication.getPrincipal();
                            User user;

                            if (principal instanceof CustomOAuth2User) {
                                user = ((CustomOAuth2User) principal).getUser();
                            } else if (principal instanceof DefaultOidcUser) {
                                DefaultOidcUser oidcUser = (DefaultOidcUser) principal;
                                String email = oidcUser.getAttribute("email");

                                Optional<User> userOptional = userRepository.findByEmail(email);

                                if (userOptional.isPresent()) {
                                    user = userOptional.get();
                                } else {
                                    user = new User();
                                    user.setEmail(email);
                                    user.setUsername(oidcUser.getAttribute("name"));
                                    user.setPassword(null);
                                    user.setAuthProvider(AuthProvider.GOOGLE);
                                    user.setRole(Role.ROLE_USER);

                                    UserProfile profile = new UserProfile();
                                    profile.setFirstName(oidcUser.getAttribute("given_name"));
                                    profile.setLastName(oidcUser.getAttribute("family_name"));
                                    profile.setUser(user);

                                    user.setProfile(profile);

                                    userRepository.save(user);
                                }
                            } else {
                                throw new IllegalArgumentException("Unsupported user type: " + principal.getClass().getName());
                            }


                            String jwt = jwtTokenUtil.generateToken(authentication);
                            response.setContentType("application/json");
                            response.getWriter().write("{\"jwt\": \"" + jwt + "\", \"email\": \"" + user.getEmail() + "\", \"username\": \"" + user.getUsername() + "\"}");
                        })





                        .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
                )


                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Collections.singletonList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Cache-Control", "Content-Type"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
