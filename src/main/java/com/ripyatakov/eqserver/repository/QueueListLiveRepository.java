package com.ripyatakov.eqserver.repository;

import com.ripyatakov.eqserver.entity.Queue;
import com.ripyatakov.eqserver.entity.QueueListLive;
import com.ripyatakov.eqserver.id_classes.QueueListLiveId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QueueListLiveRepository extends JpaRepository<QueueListLive, QueueListLiveId> {
    long countByEqQId(int eqQId);
    QueueListLive findByEqQIdAndEqUId(int eqQId, int eqUId);
    List<QueueListLive> findByEqUId(int eqUId);
}
