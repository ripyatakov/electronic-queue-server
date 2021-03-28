package com.ripyatakov.eqserver.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "queues")
public class Queue {
    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private int id;

    private int eqOwnerId;

    @Column(name = "eq_avgwt")
    private double eqAverageWaitingTime;

    private int eqCurrentUser;

    @Temporal(TemporalType.TIMESTAMP)
    private java.util.Date eqDateStart;

    @Temporal(TemporalType.TIMESTAMP)
    private java.util.Date eqDateEnd;

    private String eqType;

    private int eqMaxUsers;

    private String eqTitle;

    private String eqDescription;

}
