package com.ripyatakov.eqserver.repository;

import com.ripyatakov.eqserver.entity.Queue;
import com.ripyatakov.eqserver.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QueueRepository extends JpaRepository<Queue, Integer> {
    List<Queue> findByEqOwnerId(int eqOwnerId);
}
