package com.ripyatakov.eqserver.repository;

import com.ripyatakov.eqserver.entities.QueueListLive;
import com.ripyatakov.eqserver.id_classes.QueueListLiveId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;
import java.util.List;

public interface QueueListLiveRepository extends JpaRepository<QueueListLive, QueueListLiveId> {

    long countByEqQId(int eqQId);

    QueueListLive findByEqQIdAndEqUId(int eqQId, int eqUId);

    List<QueueListLive> findByEqUId(int eqUId);

    List<QueueListLive> findByEqQId(int eqQId);

    QueueListLive findFirstByEqQIdOrderByEqNumberDesc(int eqQId);

    QueueListLive findFirstByEqNumberGreaterThanAndEqQIdOrderByEqNumber(int eqNumber, int eqQId);

    long countByEqNumberLessThanAndEqQId(int eqNumber, int eqQId);

    long countByEqEnterTimeBetweenAndEqQId(Date d1, Date d2, int eqQId);
}
