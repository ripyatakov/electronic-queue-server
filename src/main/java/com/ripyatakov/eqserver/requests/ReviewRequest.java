package com.ripyatakov.eqserver.requests;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReviewRequest extends AuthenticationRequest{
    private float rate;
    private String description;
}
