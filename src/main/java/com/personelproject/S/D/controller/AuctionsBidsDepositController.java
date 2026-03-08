package com.personelproject.S.D.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.personelproject.S.D.model.AuctionsBidsDeposit;
import com.personelproject.S.D.service.AuctionsBidsDepositService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@RequestMapping("api/auctionsdeposits")
public class AuctionsBidsDepositController {
    @Autowired
    private AuctionsBidsDepositService auctionsBidsDepositService;


    @GetMapping("/getAll")
    public List<AuctionsBidsDeposit> getAllDeposits() {
        return auctionsBidsDepositService.findAllDeposits();
    }

    @GetMapping("/getByAuctionId/{auctionId}")
    public List<AuctionsBidsDeposit> getDepositsByAuctionId(@RequestParam String auctionId) {
        return auctionsBidsDepositService.findDepositsByAuctionId(auctionId);
     }

     @GetMapping("/id/{id}")
     public AuctionsBidsDeposit getById(@RequestParam String id) {
         return auctionsBidsDepositService.findDepositById(id);
     }
     
    




    
}

