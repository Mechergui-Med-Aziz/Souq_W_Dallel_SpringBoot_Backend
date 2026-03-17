package com.personelproject.S.D.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import com.personelproject.S.D.model.AuctionsBidsDeposit;
import com.stripe.Stripe;

@Service
public class PaymentService {

    @Value("${stripe.secret.key}")
    private String secretKey;

    @Autowired
    private AuctionsBidsDepositService auctionsBidsDepositService;

    public String createPaymentIntent(Long amount) throws Exception {

        Stripe.apiKey = secretKey;

        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(amount) // déjà en millimes
                .setCurrency("eur") // devise correcte
                .build();

        PaymentIntent intent = PaymentIntent.create(params);
        return intent.getClientSecret();
    }

    public String payAuction(String id, Double amount) throws Exception {
        AuctionsBidsDeposit deposit = new AuctionsBidsDeposit();
        deposit.setAuctionId(id);
        deposit.setAmount(amount);
        deposit.setType(AuctionsBidsDeposit.Type.AUCTION);
        AuctionsBidsDeposit saving = auctionsBidsDepositService.saveDeposit(deposit);

        if (saving != null) {
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount((long) (amount * 100)) // convertir en millimes
                    .setCurrency("eur") // devise correcte
                    .build();
            PaymentIntent intent = PaymentIntent.create(params);
            return intent.getClientSecret();

        }

        return null;

    }
}