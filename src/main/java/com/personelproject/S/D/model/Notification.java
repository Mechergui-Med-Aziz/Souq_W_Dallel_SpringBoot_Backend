package com.personelproject.S.D.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.*;
import java.time.LocalDateTime;

@Document(collection = "notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    private String id;

    private String userId;
    private String auctionId;

    private String message;

    private Type type; 

    private boolean isRead;

    private LocalDateTime createdAt;

    public enum Type {
        BID_PLACED,
        AUCTION_WON,
        AUCTION_LOST,
        AUCTION_ENDING
    }
}