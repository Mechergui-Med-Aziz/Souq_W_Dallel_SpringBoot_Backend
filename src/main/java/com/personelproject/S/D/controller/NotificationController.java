package com.personelproject.S.D.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.personelproject.S.D.model.Notification;
import com.personelproject.S.D.service.NotificationService;
import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping("api/notifications")
@RequiredArgsConstructor 
public class NotificationController {
    @Autowired
    private NotificationService notificationService;

    @GetMapping("/{userId}")
    public List<Notification> getUserNotifications(@PathVariable String userId) {
        return notificationService
                .getUserNotifications(userId);
    }

    @PutMapping("/read/{id}")
    public Notification markAsRead(@PathVariable String id) {
        Notification notification = notificationService.findById(id);
        notification.setRead(true);
        return notificationService.updateNotification(notification);
    }
}

