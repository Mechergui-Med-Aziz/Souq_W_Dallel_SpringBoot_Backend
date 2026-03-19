package com.personelproject.S.D.service;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.personelproject.S.D.model.Auction;
import com.personelproject.S.D.model.Notification;
import com.personelproject.S.D.repository.NotificationRepository;


@Service
public class NotificationService {
    @Autowired
    private NotificationRepository notificationRepository;
    @Autowired
    private AuctionService auctionService;

    public Notification save(String ownerId, String auctionId) {
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
    
    public Notification saveNotification(Notification notification) {
        return notificationRepository.save(notification);
    }

    public Notification savePaymentAdminNotification(String auctionId,Double amount) {
        Auction auction=auctionService.findAuctionById(auctionId);
        if(auction!=null){
        Notification notification = Notification.builder()
        .userId(auction.getAdminId())
        .auctionId(auctionId)
        .message("Paiement de "+amount+" DT effectué pour l'enchère dont numéro "+auctionId+" !")
        .type(Notification.Type.AUCTION_ENDING)
        .isRead(false)
        .createdAt(LocalDateTime.now())
        .build();

        return notificationRepository.save(notification);
        }
        return null;

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
