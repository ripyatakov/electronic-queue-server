package com.ripyatakov.eqserver.service;

import com.ripyatakov.eqserver.entity.Queue;
import com.ripyatakov.eqserver.entity.QueueListLive;
import com.ripyatakov.eqserver.entity.User;
import com.ripyatakov.eqserver.repository.QueueListLiveRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QueueListLiveService {
    @Autowired
    QueueListLiveRepository queueListLiveRepository;

    public int deleteAll(List<QueueListLive> queues){
        queueListLiveRepository.deleteAll(queues);
        return queues.size();
    }

    public int saveAll(List<QueueListLive> queues){
        return queueListLiveRepository.saveAll(queues).size();
    }

    public boolean registerForQueue(Queue queue, User user){
        if (queueListLiveRepository.countByEqQId(queue.getId()) < queue.getEqMaxUsers()){
            QueueListLive q = queueListLiveRepository.findFirstByEqQIdOrderByEqNumberDesc(queue.getId());
            int eqNumber = 0;
            if (q != null)
                eqNumber = q.getEqNumber() + 1;
            QueueListLive newRecord = new QueueListLive(queue.getId(), user.getId(),eqNumber);
            queueListLiveRepository.save(newRecord);
            return true;
        }
        return false;
    }

    public boolean skipAhead(Queue queue, User user){
        QueueListLive user1 = queueListLiveRepository.findByEqQIdAndEqUId(queue.getId(), user.getId());
        int a = user1.getEqNumber();
        QueueListLive user2 = queueListLiveRepository.findFirstByEqNumberGreaterThanAndEqQIdOrderByEqNumber(user1.getEqNumber(), user1.getEqQId());
        int b = user2.getEqNumber();
        user1.setEqNumber(b);
        user2.setEqNumber(a);
        queueListLiveRepository.save(user1);
        queueListLiveRepository.save(user2);
        return true;
    }

    public long queueSize(Queue queue){
        return queueListLiveRepository.countByEqQId(queue.getId());
    }

    public boolean leaveQueue(Queue queue, User user){

        QueueListLive queueList = queueListLiveRepository.findByEqQIdAndEqUId(queue.getId(), user.getId());
        if (queueList == null)
            return false;
        queueListLiveRepository.delete(queueList);
        return true;
    }

    public List<QueueListLive> getMyQueueList(User user){
        return queueListLiveRepository.findByEqUId(user.getId());
    }

    public List<QueueListLive> getQueueRecordings(Queue queue){
        return queueListLiveRepository.findByEqQId(queue.getId());
    }
}
