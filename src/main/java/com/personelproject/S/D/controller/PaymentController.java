package com.personelproject.S.D.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.personelproject.S.D.service.PaymentService;


@RestController
@RequestMapping("/api/payment")
@CrossOrigin(origins = "*")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @PostMapping("/pay1dt")
    public ResponseEntity<?> createPayment() throws Exception {

        Long amount = 1000L; // 1 DT = 1000 millimes

        String clientSecret = paymentService.createPaymentIntent(amount);
        if(clientSecret!=null)
            return ResponseEntity.ok().build();


       // Map<String, Object> response = new HashMap<>();
        //response.put("clientSecret", clientSecret);
    return ResponseEntity.badRequest().build();
        
    }
}
