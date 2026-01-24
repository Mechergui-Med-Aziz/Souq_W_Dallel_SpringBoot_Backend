package com.personelproject.S.D.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.personelproject.S.D.model.User;
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
    
        
        User savedUser = userService.saveUser(user);
    
        return ResponseEntity.ok(savedUser);
    }
    


    @PutMapping("users/update/{id}")
    public ResponseEntity<User> updateUser(@PathVariable String id, @RequestBody User user) {
        User updatedUser = userService.updateUser(id, user);
        return ResponseEntity.ok(updatedUser);
    }
    
}
