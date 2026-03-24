package com.personelproject.S.D.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.personelproject.S.D.model.AuctionsBidsDeposit;
import com.personelproject.S.D.service.AuctionsBidsDepositService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("api/auctionsdeposits")
public class AuctionsBidsDepositController {
    @Autowired
    private AuctionsBidsDepositService auctionsBidsDepositService;

    @GetMapping("/getAll")
    public List<AuctionsBidsDeposit> getAllDeposits() {
        try {
            List<AuctionsBidsDeposit> deposits = auctionsBidsDepositService.findAllDeposits();
            // Ensure createdAt is not null for frontend
            for (AuctionsBidsDeposit deposit : deposits) {
                if (deposit.getCreatedAt() == null) {
                    deposit.setCreatedAt(LocalDateTime.now());
                }
            }
            return deposits;
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    @GetMapping("/getByAuctionId")
    public List<AuctionsBidsDeposit> getDepositsByAuctionId(@RequestParam String auctionId) {
        try {
            return auctionsBidsDepositService.findDepositsByAuctionId(auctionId);
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    @GetMapping("/id/{id}")
    public AuctionsBidsDeposit getById(@PathVariable String id) {
        try {
            return auctionsBidsDepositService.findDepositById(id);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}