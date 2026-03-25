package com.personelproject.S.D.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import com.personelproject.S.D.model.AuctionsBidsDeposit;
import com.stripe.Stripe;

import jakarta.annotation.PostConstruct;

@Service
public class PaymentService {

    @Value("${stripe.secret.key}")
    private String secretKey;

    @Autowired
    private AuctionsBidsDepositService auctionsBidsDepositService;

    @PostConstruct
    public void init() {
        Stripe.apiKey = secretKey;
        System.out.println("Stripe initialized with API key");
    }

    public String createPaymentIntent(Long amount) throws Exception {
        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(amount) 
                .setCurrency("eur")
                .build();

        PaymentIntent intent = PaymentIntent.create(params);
        return intent.getClientSecret();
    }

    public String payAuction(String id, Double amount) throws Exception {
        AuctionsBidsDeposit deposit = new AuctionsBidsDeposit();
        deposit.setAuctionId(id);
        deposit.setAmount(amount*0.05);
        deposit.setType(AuctionsBidsDeposit.Type.AUCTION);
        AuctionsBidsDeposit saving = auctionsBidsDepositService.saveDeposit(deposit);

        if (saving != null) {
            Long amountInMillimes = (long) (amount * 1000);

            System.out.println("Converting amount " + amount + " TND to " + amountInMillimes + " millimes");

            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(amountInMillimes)
                    .setCurrency("eur")
                    .build();
            PaymentIntent intent = PaymentIntent.create(params);
            return intent.getClientSecret();
        }

        return null;
    }
}