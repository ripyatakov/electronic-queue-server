package com.ripyatakov.eqserver.id_classes;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Id;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QueueListLiveId  implements Serializable {
    private int eqQId;

    private int eqUId;
}
