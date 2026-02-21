package com.personelproject.S.D.service;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.personelproject.S.D.model.Notification;
import com.personelproject.S.D.repository.NotificationRepository;


@Service
public class NotificationService {
    @Autowired
    private NotificationRepository notificationRepository;

    public Notification save(String ownerId,String auctionId) {
        Notification notification = Notification.builder()
        .userId(ownerId)
        .auctionId(auctionId)
        .message("Quelqu’un a surenchéri sur votre enchère !")
        .type(Notification.Type.BID_PLACED)
        .isRead(false)
        .createdAt(LocalDateTime.now())
        .build();

        return notificationRepository.save(notification);

    }

    public List<Notification> getUserNotifications(String userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public Notification markAsRead(String id){

        Notification notification = notificationRepository.findById(id).orElseThrow();
        notification.setRead(true);
        return notificationRepository.save(notification);

    }

    public Notification findById(String id){
        return notificationRepository.findById(id).orElseThrow();
    }

    public Notification updateNotification(Notification notification){
        return notificationRepository.save(notification);
    }
    
}
