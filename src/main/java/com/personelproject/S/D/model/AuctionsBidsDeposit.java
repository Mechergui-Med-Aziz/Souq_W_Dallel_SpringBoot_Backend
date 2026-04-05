package com.personelproject.S.D.model;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection = "deposits")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuctionsBidsDeposit {

    @Id
    private String id;

    private String auctionId;
    private Type type; 
    private Double amount;
    private LocalDateTime createdAt;
    
    
    public enum Type{
        AUCTION, BIDS,CREATION
    }

    
}

    
