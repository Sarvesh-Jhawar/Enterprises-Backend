package com.tech.enterprise.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Spring Security 6+ configuration for session-based authentication.
 * 
 * Security rules:
 * - Permit: POST /api/{tenantSlug}/admins/login
 * - Secure: All other /api/** endpoints require authentication
 * - Session-based authentication (default Spring Security behavior)
 * - CSRF disabled for stateless API usage
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http
                                // Disable CSRF for REST API
                                .csrf(csrf -> csrf.disable())

                                // Configure CORS
                                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                                // Authorization rules
                                .authorizeHttpRequests(auth -> auth
                                                // Permit login endpoint
                                                .requestMatchers(HttpMethod.POST, "/api/*/admins/login").permitAll()
                                                // Permit logout endpoint
                                                .requestMatchers(HttpMethod.POST, "/api/*/admins/logout").permitAll()
                                                // Permit actuator health endpoint
                                                .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                                                // Permit GET requests to product endpoints (public access)
                                                .requestMatchers(HttpMethod.GET, "/api/*/products/**").permitAll()
                                                // Secure all other API endpoints
                                                .requestMatchers("/api/**").authenticated()
                                                // Permit everything else (static resources, etc.)
                                                .anyRequest().permitAll())

                                // Use default session management (session-based auth)
                                .sessionManagement(session -> session
                                                .maximumSessions(1)
                                                .maxSessionsPreventsLogin(false))

                                // Disable default form login
                                .formLogin(form -> form.disable())

                                // Disable HTTP Basic auth
                                .httpBasic(basic -> basic.disable());

                return http.build();
        }

        @Bean
        public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
                        throws Exception {
                return config.getAuthenticationManager();
        }

        @Bean
        public CorsConfigurationSource corsConfigurationSource() {
                CorsConfiguration configuration = new CorsConfiguration();

                // Hardcoded allowed origins - update this list when deploying your frontend
                configuration.setAllowedOrigins(List.of(
                                "http://localhost:5173",
                                "https://shobha-enterprise.vercel.app"));

                configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                configuration.setAllowedHeaders(List.of("*"));
                configuration.setAllowCredentials(true);
                configuration.setMaxAge(3600L);

                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", configuration);
                return source;
        }
}
