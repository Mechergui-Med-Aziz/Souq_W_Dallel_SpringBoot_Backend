package com.personelproject.S.D.model;


import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class User {
    

    @Id
    private String id;

    private String firstname;
    private String lastname;

    private Integer cin;

    private String email;
    private String password;

    private String role;
    private String status;

}

    

