package com.personelproject.S.D.service;

import java.util.ArrayList;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.personelproject.S.D.model.User;
import com.personelproject.S.D.repository.UserRepository;

@Service
public class UserService implements UserDetailsService{

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User findUserByEmail(String email) {
        Optional<User> userByEmail = userRepository.findByEmail(email);
        if (userByEmail.isPresent()) {
            return userByEmail.get();
        }
        return null;
    }

    public User findUserByEmailAndCin(String email, Integer cin) {
        Optional<User> userByEmail = userRepository.findByEmail(email);
        if (userByEmail.isPresent()) {
            return userByEmail.get();
        }
    
        Optional<User> userByCin = userRepository.findByCin(cin);
        if (userByCin.isPresent()) {
            return userByCin.get();
        }
    
        return null; 
    }
    
    

    public User saveUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

   

    public User findUserById(String id) {
        return userRepository.findById(id)
            .orElseThrow(() ->new ResponseStatusException(HttpStatus.NOT_FOUND,"User not found with id: " + id));
    }
    

    public void deleteUserById(String id) {
        userRepository.deleteById(id);
    }

    public User updateUser(String id, User updatedUser) {
        User existingUser = findUserById(id);
        existingUser.setFirstname(updatedUser.getFirstname());
        existingUser.setLastname(updatedUser.getLastname());
        existingUser.setCin(updatedUser.getCin());
        existingUser.setEmail(updatedUser.getEmail());
        if (updatedUser.getPassword() != null && !updatedUser.getPassword().isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
        }
        existingUser.setRole(updatedUser.getRole());
        return userRepository.save(existingUser);
    }


 
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user=findUserByEmail(email);
        return new org.springframework.security.core.userdetails.User(user.getEmail(), user.getPassword(), new ArrayList<>());
    }
    }
    

