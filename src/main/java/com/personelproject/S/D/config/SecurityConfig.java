package com.personelproject.S.D.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;


import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.personelproject.S.D.security.JwtAuthenticationFilter;
import com.personelproject.S.D.security.JwtAuthorizationFilter;
import com.personelproject.S.D.security.JwtUtils;
import com.personelproject.S.D.service.UserService;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    private final UserService userService;
    private final JwtUtils jwtUtils;

    public SecurityConfig(UserService userService, JwtUtils jwtUtils) {
        this.userService = userService;
        this.jwtUtils = jwtUtils;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

  

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, AuthenticationManager authenticationManager) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource())) // autorise les appels depuis http://localhost:4200
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                // toutes les requêtes sont autorisées sauf celles vers /cmpx/**
                .requestMatchers("/api/payment/**").authenticated()
                .anyRequest().permitAll()
            )
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // JWT uniquement
            .authenticationManager(authenticationManager);
    
        // Ajout des filtres de sécurité
        http.addFilter(new JwtAuthenticationFilter(authenticationManager, jwtUtils, userService));
        http.addFilterBefore(new JwtAuthorizationFilter(authenticationManager, jwtUtils), UsernamePasswordAuthenticationFilter.class);
    
        return http.build();
    }
    

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.setAllowedOriginPatterns(List.of("http://localhost:8082")); // ///////////////////////////////
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
