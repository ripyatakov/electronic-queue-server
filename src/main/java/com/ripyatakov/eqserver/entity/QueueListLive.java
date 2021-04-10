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
public class QueueListLive implements Comparable {
    @Id
    @Column(name = "eq_qid")
    private int eqQId;
    @Id
    @Column(name = "eq_uid")
    private int eqUId;

    private int eqNumber;

    @Temporal(TemporalType.TIMESTAMP)
    protected java.util.Date eqEnterTime;

    private float eqServeTimeMin;

    @Temporal(TemporalType.TIMESTAMP)
    protected java.util.Date eqStartServeTime;

    @Override
    public int compareTo(Object o) {
        QueueListLive obj = (QueueListLive)o;
        if (equals(obj))
            return 0;
        return Integer.compare(eqNumber, obj.getEqNumber());
    }

    @Override
    public boolean equals(Object o){
        QueueListLive obj = (QueueListLive)o;
        return (getEqUId() == obj.getEqUId()) && (getEqQId() == obj.getEqQId());
    }
}
