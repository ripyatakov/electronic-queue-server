package com.ripyatakov.eqserver.repository;

import com.ripyatakov.eqserver.entity.Review;
import com.ripyatakov.eqserver.id_classes.QueueListLiveId;
import com.ripyatakov.eqserver.id_classes.ReviewId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, ReviewId> {
    Review findByRwUIdAndRwQId(int uid, int qid);

    List<Review> findAllByRwUId(int uid);

    List<Review> findAllByRwQId(int qid);


}
