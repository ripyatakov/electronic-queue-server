package com.ripyatakov.eqserver.json;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserInQueue {
    private int id;
    private int queuePlace;
    private String name;
}
