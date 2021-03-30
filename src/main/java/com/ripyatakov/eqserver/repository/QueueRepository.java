package com.ripyatakov.eqserver.repository;

import com.ripyatakov.eqserver.entity.Queue;
import com.ripyatakov.eqserver.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;
import java.util.List;

public interface QueueRepository extends JpaRepository<Queue, Integer> {
    List<Queue> findByEqOwnerId(int eqOwnerId);

    List<Queue> findAllByEqDateStartBetween(Date d1, Date d2);
}
