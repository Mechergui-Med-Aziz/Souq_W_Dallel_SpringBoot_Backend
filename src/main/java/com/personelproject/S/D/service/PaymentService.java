package com.personelproject.S.D.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.Stripe;

@Service
public class PaymentService {

    @Value("${stripe.secret.key}")
    private String secretKey;

    public String createPaymentIntent(Long amount) throws Exception {

        Stripe.apiKey = secretKey;

        PaymentIntentCreateParams params =
                PaymentIntentCreateParams.builder()
                        .setAmount(amount) // déjà en millimes
                        .setCurrency("eur") // devise correcte
                        .build();

        PaymentIntent intent = PaymentIntent.create(params);
        return intent.getClientSecret();
    }
}