package com.personelproject.S.D.security;


import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tools.jackson.databind.ObjectMapper;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.personelproject.S.D.model.User;
import com.personelproject.S.D.service.EmailService;
import com.personelproject.S.D.service.UserService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final UserService userService ;
    private final EmailService emailService;

    public JwtAuthenticationFilter(AuthenticationManager authenticationManager, JwtUtils jwtUtils, UserService userService, EmailService emailService) {
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
        this.userService = userService;
        this.emailService = emailService;
        setFilterProcessesUrl("/api/auth/login");
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        try {
            User creds = new ObjectMapper().readValue(request.getInputStream(), User.class);
            return authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            creds.getEmail(),
                            creds.getPassword(),
                            new ArrayList<>()
                    )
            );
        } catch (IOException e) {
            throw new RuntimeException("Failed to read credentials", e);
        }
    }
    

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, 
                                            FilterChain chain, Authentication authResult) 
                                            throws IOException, ServletException {
    
        User user = userService.findUserByEmail(authResult.getName());
    
        // Vérification du statut "Blocked"
        if ("Blocked".equalsIgnoreCase(user.getStatus())) {
            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.setContentType("application/json");
    
            Map<String, String> responseMap = new HashMap<>();
            responseMap.put("error", "Votre compte est bloqué ! Veuillez contacter l'administrateur.");
            new ObjectMapper().writeValue(response.getOutputStream(), responseMap);
            return; // arrêter ici la méthode
        }
    
        // Si le statut est "Waiting for validation"
        Map<String, String> responseMap = new HashMap<>();
        if ("Waiting for validation".equalsIgnoreCase(user.getStatus())) {
           
            String code=emailService.sendConfirmationCode(user.getEmail());
            responseMap.put("code",code);
        }
        if(!"Waiting for validation".equalsIgnoreCase(user.getStatus())) {
            String token = jwtUtils.generateToken(authResult.getName());
            responseMap.put("token", token);
        }
    
        // Génération du token JWT
        
        
        responseMap.put("email", user.getEmail());
        responseMap.put("role", user.getRole());
        responseMap.put("status", user.getStatus());
        responseMap.put("id", String.valueOf(user.getId()));
    
        response.setContentType("application/json");
        response.setStatus(HttpStatus.OK.value());
        new ObjectMapper().writeValue(response.getOutputStream(), responseMap);
    }
    

@Override
protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
                                          AuthenticationException failed)
        throws IOException, ServletException {
    response.setStatus(HttpStatus.UNAUTHORIZED.value());
    response.setContentType("application/json");
    
    Map<String, String> responseMap = new HashMap<>();
    responseMap.put("error", "Nom d'utilisateur ou mot de passe incorrect !");
    
    new ObjectMapper().writeValue(response.getOutputStream(), responseMap);
}

}