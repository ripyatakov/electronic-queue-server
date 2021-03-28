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

    public boolean registerForQueue(Queue queue, User user){
        if (queueListLiveRepository.countByEqQId(queue.getId()) < queue.getEqMaxUsers()){
            QueueListLive newRecord = new QueueListLive(queue.getId(), user.getId(),0);
            queueListLiveRepository.save(newRecord);
            return true;
        }
        return false;
    }

    public long queueCapacity(Queue queue){
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
}
