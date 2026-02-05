package com.personelproject.S.D.model;
import lombok.*;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "users")
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

    private String status;

    private List<String> photoId;
    private List<User> bidders;
    private User seller;
    
}
