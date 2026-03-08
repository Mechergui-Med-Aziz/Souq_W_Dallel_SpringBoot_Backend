package com.personelproject.S.D.service;


import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.personelproject.S.D.model.AuctionsBidsDeposit;
import com.personelproject.S.D.repository.AuctionsBidsDepositRepository;

@Service
public class AuctionsBidsDepositService {
    @Autowired
    private AuctionsBidsDepositRepository auctionsBidsDepositRepository;

    public AuctionsBidsDeposit saveDeposit(AuctionsBidsDeposit deposit) {
        return auctionsBidsDepositRepository.save(deposit);
    }

    public AuctionsBidsDeposit findDepositById(String id) {
        return auctionsBidsDepositRepository.findById(id).orElse(null);
    }

    public AuctionsBidsDeposit findDepositsByAuctionIdAndType(String auctionId,String type) {
        return auctionsBidsDepositRepository.findByAuctionIdAndType(auctionId,type);
    }
    public List<AuctionsBidsDeposit> findDepositsByAuctionId(String auctionId) {
        return auctionsBidsDepositRepository.findByAuctionId(auctionId);
    }
    

    public List<AuctionsBidsDeposit> findAllDeposits() {
        return auctionsBidsDepositRepository.findAll();
    }

    public AuctionsBidsDeposit updaDeposit(AuctionsBidsDeposit deposit) {
        if (auctionsBidsDepositRepository.existsById(deposit.getId())) {
            return auctionsBidsDepositRepository.save(deposit);
        } else {
            return null; // Or throw an exception
        }
    }
    
}

