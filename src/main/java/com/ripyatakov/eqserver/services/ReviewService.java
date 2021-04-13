package com.ripyatakov.eqserver.services;

import com.ripyatakov.eqserver.entities.Queue;
import com.ripyatakov.eqserver.entities.Review;
import com.ripyatakov.eqserver.entities.User;
import com.ripyatakov.eqserver.repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ReviewService {
    @Autowired
    ReviewRepository repository;

    public Review saveReview(Review review){
        return repository.save(review);
    }

    public List<Review> findAllByUid(User user){
        return repository.findAllByRwUId(user.getId());
    }

    public List<Review> findAllByUid(int uid){
        return repository.findAllByRwUId(uid);
    }

    public List<Review> findAllByQid(Queue queue){
        return repository.findAllByRwQId(queue.getId());
    }

    public List<Review> findAllByQid(int qid){
        return repository.findAllByRwQId(qid);
    }

    public List<Review> findAllByQids(List<Integer> qids){
        List<Review> lst = new ArrayList<>();
        for (int qid: qids){
            lst.addAll(findAllByQid(qid));
        }
        return lst;
    }

    public List<Review> findAllByUids(List<Integer> uids){
        List<Review> lst = new ArrayList<>();
        for (int uid: uids){
            lst.addAll(findAllByUid(uid));
        }
        return lst;
    }

    public double getAverageRating(Queue q){
        try {
            return findAllByQid(q.getId()).stream().mapToDouble(a -> a.getRate()).average().getAsDouble();
        } catch (Exception exc){
            return -1;
        }
    }
}
