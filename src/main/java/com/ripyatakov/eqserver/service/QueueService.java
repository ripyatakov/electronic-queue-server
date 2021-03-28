package com.ripyatakov.eqserver.service;

import com.ripyatakov.eqserver.entity.Queue;
import com.ripyatakov.eqserver.repository.QueueRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QueueService {
    @Autowired
    QueueRepository repository;

    public Queue saveQueue(Queue queue){
        return repository.save(queue);
    }

    public List<Queue> saveQueues(List<Queue> queues){
        return repository.saveAll(queues);
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

    public String deleteQueue(int id){
        repository.deleteById(id);
        return "Queue deleted " + id;
    }



}
