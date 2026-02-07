package com.personelproject.S.D.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.personelproject.S.D.model.Auction;
import com.personelproject.S.D.repository.AuctionRepository;

@Service
public class AuctionService {

    @Autowired
    private AuctionRepository auctionRepository;

    public Auction saveAuction(Auction auction){
        return auctionRepository.save(auction);

    }

    public Auction findAuctionById(String id) {
        return auctionRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Auction not found with id: " + id
            ));
    }
    public List<Auction> findAllAuctions() {
        return auctionRepository.findAll();
    }
    
    public void deleteAuction(String id) {
        if (!auctionRepository.existsById(id)) {
            throw new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Auction not found with id: " + id
            );
        }
        auctionRepository.deleteById(id);
    }

    public Auction updateAuction(Auction auction) {
        if (!auctionRepository.existsById(auction.getId())) {
            throw new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Auction not found with id: " + auction.getId()
            );
        }
        return auctionRepository.save(auction);
    }
    

    
}
