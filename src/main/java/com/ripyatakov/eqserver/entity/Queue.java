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
    protected int id;

    protected int eqOwnerId;

    @Column(name = "eq_avgwt")
    protected double eqAverageWaitingTime;

    protected int eqCurrentUser;

    @Temporal(TemporalType.TIMESTAMP)
    protected java.util.Date eqDateStart;

    @Temporal(TemporalType.TIMESTAMP)
    protected java.util.Date eqDateEnd;

    protected String eqType;

    protected int eqMaxUsers;

    protected String eqTitle;

    protected String eqDescription;

    protected String eqStatus;

}
