package com.personelproject.S.D.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

import com.personelproject.S.D.model.User;
import com.personelproject.S.D.model.UserResetPasswordRequest;
import com.personelproject.S.D.service.EmailService;
import com.personelproject.S.D.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping("api")

public class UserController {

    @Autowired
    private UserService userService;
    @Autowired
    private EmailService emailService;

    @GetMapping("users/id/{id}")
    public ResponseEntity<User> getUserById(@PathVariable String id) {
        return ResponseEntity.ok(userService.findUserById(id));
    }

    @PostMapping("/auth/register")
    public ResponseEntity<?> createUser(@RequestBody User user) {
    
        User existingUser = userService.findUserByEmailAndCin(user.getEmail(), user.getCin());
    
        if (existingUser != null) {
    
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("message", "Un utilisateur avec cet email ou ce CIN existe déjà !"));
        }
        user.setRole("User");
        user.setStatus("Waiting for validation");
     
        User savedUser = userService.saveUser(user);
        String code =emailService.sendConfirmationCode(savedUser.getEmail());
    
        return ResponseEntity.ok(Map.of("user", savedUser, "code", code));
    }

    @PostMapping("/auth/confirmation/{email}")
    public ResponseEntity<?> validateAccount(@PathVariable String email) {
        User user = userService.findUserByEmail(email);
        System.out.println("µµµµµµµµµµµµµµµµµµµµµµµµµµµµµµµµµµ%%%%%%%%%%%%%%"+email);
        if (user == null) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("message", "Utilisateur non trouvé !"));
        }
        user.setRole("User");
        user.setStatus("Activated");
        user.setPassword("");
        User updatedUser = userService.updateUser(user.getId(), user);
        if( updatedUser!= null)
            emailService.sendActivationAccountEmail(email);
        
        return ResponseEntity.ok(updatedUser);
    }

    @PostMapping("/reset-password")
public ResponseEntity<Map<String, Object>> resetPassword(@RequestBody UserResetPasswordRequest request) {
    User user = userService.findUserByEmailAndCin( request.getEmail(),request.getCin());

    if (user != null) {
        System.out.println(user);
        emailService.sendPasswordResetEmail(request.getEmail(), user);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Un email de réinitialisation de mot de passe a été envoyé, Veuillez vérifier votre boîte de réception.");
        
        return ResponseEntity.ok(response);
    }

    Map<String, Object> errorResponse = new HashMap<>();
    errorResponse.put("success", false);
    errorResponse.put("message", "Aucun utilisateur trouvé avec ce cin et cet email.");
    
    return ResponseEntity.badRequest().body(errorResponse);
}
    
    


    @PutMapping("users/update/{id}")
    public ResponseEntity<User> updateUser(@PathVariable String id, @RequestBody User user) {
        User updatedUser = userService.updateUser(id, user);
        return ResponseEntity.ok(updatedUser);
    }
    
}
