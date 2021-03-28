package com.ripyatakov.eqserver.entity;

import com.ripyatakov.eqserver.id_classes.QueueListLiveId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "queuelistlive")
@IdClass(QueueListLiveId.class)
public class QueueListLive {
    @Id
    @Column(name = "eq_qid")
    private int eqQId;
    @Id
    @Column(name = "eq_uid")
    private int eqUId;

    private int eqNumber;
}
