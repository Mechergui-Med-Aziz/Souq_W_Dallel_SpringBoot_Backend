package com.personelproject.S.D.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.personelproject.S.D.model.User;



public interface UserRepository extends MongoRepository<User, String> {
    
    Optional<User> findByEmail(String email);

    Optional<User> findById(String id);

    User findByCin(int cin);



}
