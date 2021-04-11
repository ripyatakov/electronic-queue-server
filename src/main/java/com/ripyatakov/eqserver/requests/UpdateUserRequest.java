package com.ripyatakov.eqserver.requests;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateUserRequest extends AuthenticationRequest{
    private String email;
    private String password;
    private String name;
}
