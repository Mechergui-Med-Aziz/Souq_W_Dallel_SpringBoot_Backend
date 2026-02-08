package com.personelproject.S.D.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.personelproject.S.D.model.Auction;

@Repository
public interface AuctionRepository extends  MongoRepository<Auction, String> {

     
    Optional<Auction> findById(String id);
    List<Auction> findBySellerId(String sellerId);
    List<Auction> findByCategory(String category);
    List<Auction> findAll();

    List<Auction> findBySeller(String id);
        
   
}
