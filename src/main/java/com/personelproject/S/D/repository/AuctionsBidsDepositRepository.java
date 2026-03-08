
package com.personelproject.S.D.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.personelproject.S.D.model.AuctionsBidsDeposit;

@Repository
public interface AuctionsBidsDepositRepository extends  MongoRepository<AuctionsBidsDeposit, String> {

     
    Optional<AuctionsBidsDeposit> findById(String id);
    AuctionsBidsDeposit findByAuctionIdAndType(String auctionId, String type);
    List<AuctionsBidsDeposit> findAll();  

    List<AuctionsBidsDeposit> findByAuctionId(String auctionId);
    
    
}
