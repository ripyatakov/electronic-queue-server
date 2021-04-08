package com.ripyatakov.eqserver.id_classes;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReviewId  implements Serializable {
    private int rwQId;

    private int rwUId;
}