package com.ripyatakov.eqserver.entity;

import com.ripyatakov.eqserver.id_classes.QueueListLiveId;
import com.ripyatakov.eqserver.id_classes.ReviewId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "reviews")
@IdClass(ReviewId.class)
public class Review {
    @Id
    @Column(name = "rw_qid")
    private int rwQId;
    @Id
    @Column(name = "rw_uid")
    private int rwUId;

    @Column(name = "rw_rate")
    private float rate;

    @Column(name = "rw_description")
    private String description;

    @Column(name = "rw_time")
    @Temporal(TemporalType.TIMESTAMP)
    protected java.util.Date time;

}