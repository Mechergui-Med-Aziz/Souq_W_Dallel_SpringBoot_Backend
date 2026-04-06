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
                auctionService.updateAuction(auction);
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

            Auction auction = auctionService.findAuctionById(auctionId);
            if (auction == null) {
                return ResponseEntity.status(404).body(Map.of("error", "Auction not found"));
            }

            // Process payment through Stripe
            String clientSecret = paymentService.payAuction(auctionId, amount);

            if (clientSecret != null) {
                // Mark auction as paid in database
                auction.setPaid(true);
                auctionService.updateAuction(auction);
                System.out.println("Auction " + auctionId + " marked as paid");

                Map<String, String> response = new HashMap<>();
                response.put("clientSecret", clientSecret);

                // Create parcel for the auction (only if payment successful)
                try {
                    Notification notif = notificationService.savePaymentAdminNotification(auctionId, amount);
                    if (notif != null) {
                        Parcel parcel = new Parcel();
                        parcel.setAuctionId(auctionId);
                        String adminId = auction.getAdminId();
                        parcel.setAdminId(adminId);
                        parcel.setBuyerId(auctionService.getBuyer(auctionId).getId());
                        parcel.setIsValid(null);
                        parcel.setDestinationAdress(null);
                        parcel.setPickUpAdress(null);
                        parcel.setUnvalidDescription(null);
                        parcel.setTransporterId(null);
                        parcel.setDelivred(false);
                        parcel = parcelService.saveParcel(parcel);
                        System.out.println("Created parcel with id: " + parcel.getId() + " for auction: " + auctionId);
                    }
                } catch (Exception e) {
                    System.err.println("Failed to create parcel: " + e.getMessage());
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