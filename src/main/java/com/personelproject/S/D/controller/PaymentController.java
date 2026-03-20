package com.personelproject.S.D.controller;

import com.personelproject.S.D.service.NotificationService;
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

    @PostMapping("/payAuction/{auctionId}/{amount}")
    public ResponseEntity<?> payAuction(@PathVariable String auctionId, @PathVariable Double amount) {
        try {
            System.out.println("Processing payment for auction: " + auctionId + " amount: " + amount);

            String clientSecret = paymentService.payAuction(auctionId, amount);

            if (clientSecret != null) {
                Map<String, String> response = new HashMap<>();
                response.put("clientSecret", clientSecret);

                // Send notification to admin
                try {
                    notificationService.savePaymentAdminNotification(auctionId, amount);
                } catch (Exception e) {
                    System.err.println("Failed to send admin notification: " + e.getMessage());
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