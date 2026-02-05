package com.personelproject.S.D.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.personelproject.S.D.model.User;



@Repository
public interface UserRepository extends MongoRepository<User, String> {
    
    Optional<User> findByEmail(String email);
    Optional<User> findByCin(Integer cin);

    Optional<User> findById(String id);

    User findByCin(int cin);



}
