package com.personelproject.S.D.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "parcels")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Parcel {

    @Id
    private String id;

    private String auctionId;
    private Boolean isValid;
    private String buyerId;
    private boolean isDelivred;

    private String transporterId;
    private String pickUpAdress;
    private String unvalidDescription;
    private String destinationAdress;
    private String adminId;

}
