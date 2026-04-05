package com.personelproject.S.D.controller;

import com.personelproject.S.D.model.Auction;
import com.personelproject.S.D.model.AuctionsBidsDeposit;
import com.personelproject.S.D.model.Notification;
import com.personelproject.S.D.model.Parcel;
import com.personelproject.S.D.service.AuctionService;
import com.personelproject.S.D.service.AuctionsBidsDepositService;
import com.personelproject.S.D.service.NotificationService;
import com.personelproject.S.D.service.ParcelService;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.personelproject.S.D.service.PaymentService;

@RestController
@RequestMapping("/api/payment")
@CrossOrigin(origins = "*")
public class PaymentController {

    @Autowired
    private NotificationService notificationService;
    @Autowired
    private PaymentService paymentService;
    @Autowired
    private AuctionService auctionService;
    @Autowired
    private ParcelService parcelService;
    @Autowired
    private AuctionsBidsDepositService auctionsBidsDepositService;

    @PostMapping("/pay1dt")
    public ResponseEntity<?> createPayment() {
        try {
            Long amount = 1000L; // 1 DT = 1000 millimes
            String clientSecret = paymentService.createPaymentIntent(amount);

            if (clientSecret != null) {
                Map<String, String> response = new HashMap<>();
                response.put("clientSecret", clientSecret);
                return ResponseEntity.ok(response);
            }

            return ResponseEntity.badRequest().body(Map.of("error", "Failed to create payment intent"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/payCreationAuctionFees/{auctionId}/{amount}")
    public ResponseEntity<?> createCreationFeesPayment(@PathVariable String auctionId, @PathVariable Long amount) {
        try {
            String clientSecret = paymentService.createPaymentIntent(amount);

            if (clientSecret != null) {
                Map<String, String> response = new HashMap<>();
                response.put("clientSecret", clientSecret);
                Auction auction = auctionService.findAuctionById(auctionId);
                auction.setStatus("pending");
                AuctionsBidsDeposit deposit = new AuctionsBidsDeposit();
                deposit.setAuctionId(auctionId);
                deposit.setAmount(amount.doubleValue());
                deposit.setType(AuctionsBidsDeposit.Type.CREATION);
                auctionsBidsDepositService.saveDeposit(deposit);
                return ResponseEntity.ok(response);
            }

            return ResponseEntity.badRequest().body(Map.of("error", "Failed to create payment intent"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/payAuction/{auctionId}/{amount}")
    public ResponseEntity<?> payAuction(@PathVariable String auctionId, @PathVariable Double amount) {
        try {
            System.out.println("Processing payment for auction: " + auctionId + " amount: " + amount);

            String clientSecret = paymentService.payAuction(auctionId, amount);

            if (clientSecret != null) {
                Map<String, String> response = new HashMap<>();
                response.put("clientSecret", clientSecret);

                try {
                    Notification notif = notificationService.savePaymentAdminNotification(auctionId, amount);
                    if (notif != null) {
                        Auction auction = auctionService.findAuctionById(auctionId);
                        Parcel parcel = new Parcel();
                        parcel.setAuctionId(auctionId);
                        // Set the adminId from the auction (the admin who approved it)
                        String adminId = auction.getAdminId();
                        if (adminId == null) {
                            // If auction doesn't have adminId (legacy), try to get from the notification or
                            // set null
                            // For now, set to null and admins will see it in dashboard
                            System.err.println("Warning: Auction " + auctionId + " has no adminId set");
                        }
                        parcel.setAdminId(adminId);
                        parcel.setBuyerId(auctionService.getBuyer(auctionId).getId());
                        parcel.setIsValid(null);
                        parcel.setDestinationAdress(null);
                        parcel.setPickUpAdress(null);
                        parcel.setUnvalidDescription(null);
                        parcel.setTransporterId(null);
                        parcel.setDelivred(false);
                        parcel = parcelService.saveParcel(parcel);
                        System.out.println("Created parcel with adminId: " + adminId);
                    }
                } catch (Exception e) {
                    System.err.println("Failed to send admin notification: " + e.getMessage());
                    e.printStackTrace();
                }

                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(Map.of("error", "Failed to create payment intent"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
}