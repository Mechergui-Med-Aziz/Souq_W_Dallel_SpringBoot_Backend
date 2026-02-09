package com.personelproject.S.D.model;
import lombok.*;

import java.util.List;
import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "auctions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Auction {
    
    @Id
    private String id;

    private String title;
    private String description;

    private Double startingPrice;
    private String category;

    private String status;

    private List<String> photoId;
    private Map<String,Double> bidders;
    private User seller;
    
}
