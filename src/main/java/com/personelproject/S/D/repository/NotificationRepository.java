package com.personelproject.S.D.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.personelproject.S.D.model.Notification;

import java.util.List;

@Repository
public interface NotificationRepository 
        extends MongoRepository<Notification, String> {

    List<Notification> findByUserIdOrderByCreatedAtDesc(String userId);
}