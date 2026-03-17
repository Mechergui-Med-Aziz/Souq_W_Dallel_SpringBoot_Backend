package com.personelproject.S.D.model;

import lombok.*;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.util.MultiValueMap;

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
    private Map<String, Double> bidders;
    private String sellerId;
    private Date expireDate;
    private MultiValueMap<String, String> reviews;
    private String adminId;
}