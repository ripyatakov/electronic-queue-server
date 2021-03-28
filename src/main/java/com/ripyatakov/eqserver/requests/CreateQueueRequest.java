package com.ripyatakov.eqserver.requests;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateQueueRequest {

    private String token;

    private String title;
    private String description;

    private int maxUsers;

    private java.util.Date dateStart;
    private java.util.Date dateEnd;
}
