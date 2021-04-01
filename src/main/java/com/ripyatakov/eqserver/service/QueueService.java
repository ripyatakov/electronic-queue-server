package com.ripyatakov.eqserver.service;

import com.ripyatakov.eqserver.entity.Queue;
import com.ripyatakov.eqserver.entity.QueueListLive;
import com.ripyatakov.eqserver.repository.QueueRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
public class QueueService {
    @Autowired
    QueueRepository repository;

    public Queue saveQueue(Queue queue){
        return repository.save(queue);
    }

    public int saveQueues(List<Queue> queues){
        return repository.saveAll(queues).size();
    }

    public Queue getQueueById(int id){
        return repository.findById(id).orElse(null);
    }

    public List<Queue> getAllQueues(){
        return repository.findAll();
    }

    public List<Queue> getQueuesByOwnerId(int ownerId){
        return repository.findByEqOwnerId(ownerId);
    }

    public Queue updateQueue(Queue queue){
        Queue existingQueue = repository.findById(queue.getId()).orElse(null);
        existingQueue.setEqCurrentUser(queue.getEqCurrentUser());
        existingQueue.setEqDateEnd(queue.getEqDateEnd());
        existingQueue.setEqDescription(queue.getEqDescription());
        existingQueue.setEqDateStart(queue.getEqDateStart());
        existingQueue.setEqAverageWaitingTime(queue.getEqAverageWaitingTime());
        existingQueue.setEqMaxUsers(queue.getEqMaxUsers());
        existingQueue.setEqTitle(queue.getEqTitle());
        existingQueue.setEqOwnerId(queue.getEqOwnerId());
        existingQueue.setEqType(queue.getEqType());
        return repository.save(existingQueue);
    }

    public List<Queue> getQueuesFromList(List<QueueListLive> list){
        List<Queue> queues = new ArrayList<>();
        for (QueueListLive q: list){
            queues.add(getQueueById(q.getEqQId()));
        }
        return queues;
    }

    public String deleteQueue(int id){
        repository.deleteById(id);
        return "Queue deleted " + id;
    }

    public List<Queue> todayQueues(){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        Date d1 = calendar.getTime();
        calendar.add(Calendar.DATE, 1);
        Date d2 = calendar.getTime();
        return repository.findAllByEqDateStartBetween(d1, d2);

    }


}
