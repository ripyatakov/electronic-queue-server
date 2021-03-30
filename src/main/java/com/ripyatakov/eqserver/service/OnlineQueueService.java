package com.ripyatakov.eqserver.service;

import com.ripyatakov.eqserver.entity.Queue;
import com.ripyatakov.eqserver.entity.QueueListLive;
import com.ripyatakov.eqserver.entity.User;
import com.ripyatakov.eqserver.pdo.OnlineQueue;
import com.ripyatakov.eqserver.pdo.OnlineQueueLive;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OnlineQueueService {

    HashMap<Integer, OnlineQueueLive> onlineQueues = new HashMap<>();

    public boolean isOnline(int qid){
        return onlineQueues.containsKey(qid);
    }

    public boolean isOnline(Queue q){
        return onlineQueues.containsKey(q.getId());
    }

    public void clearInactiveQueues(){
        for (OnlineQueue q: onlineQueues.values()) {
            if (!q.isActive()){
                onlineQueues.remove(q);
            }
        }
    }

    public void clearDeletedRecords(){
        for (OnlineQueue q: onlineQueues.values()){
            q.update();
        }
    }

    public void loadOnlineLiveQueue(Queue queueInfo, List<QueueListLive> queue){
        if (queue.isEmpty())
            return;
        int qid = queueInfo.getId();
        OnlineQueueLive queueLive = new OnlineQueueLive(queueInfo, queue);
        onlineQueues.put(qid, queueLive);
    }

    public synchronized boolean registerForQueue(Queue queue, User user){
        if (isOnline(queue)){
            onlineQueues.get(queue.getId()).registerForQueue(user);
            return true;
        }
        return false;
    }

    public synchronized boolean leaveQueue(Queue queue, User user){
        if (isOnline(queue)){
            return onlineQueues.get(queue.getId()).leaveQueue(user);
        }
        return false;
    }

    public synchronized List<QueueListLive> dataToSave(){
        return onlineQueues.values()
                .stream().map(OnlineQueueLive::dataToSave)
                .flatMap(List::stream)
                .filter(a -> a != null)
                .collect(Collectors.toList());
    }
    public synchronized List<QueueListLive> dataToDelete(){
        return onlineQueues.values()
                .stream().map(OnlineQueueLive::dataToDelete)
                .flatMap(List::stream)
                .filter(a -> a != null)
                .collect(Collectors.toList());
    }
}
