package com.personelproject.S.D.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserResetPasswordRequest {
    private Integer cin;
    private String email;
}
