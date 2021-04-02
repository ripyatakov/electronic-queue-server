package com.ripyatakov.eqserver.service;

import com.ripyatakov.eqserver.dto.OnlineQueueData;
import com.ripyatakov.eqserver.entity.Queue;
import com.ripyatakov.eqserver.entity.QueueListLive;
import com.ripyatakov.eqserver.entity.User;
import com.ripyatakov.eqserver.pdo.OnlineQueue;
import com.ripyatakov.eqserver.pdo.OnlineQueueLive;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class OnlineQueueService {

    Map<Integer, OnlineQueueLive> onlineQueues = Collections.synchronizedMap(new HashMap<>());

    public synchronized boolean isOnline(int qid){
        return onlineQueues.containsKey(qid);
    }

    public synchronized boolean isOnline(Queue q){
        return onlineQueues.containsKey(q.getId());
    }

    public synchronized void clearInactiveQueues(){
        for (OnlineQueue q: onlineQueues.values()) {
            if (!q.isActive()){
                onlineQueues.remove(q);
            }
        }
    }

    public synchronized void clearDeletedRecords(){
        for (OnlineQueue q: onlineQueues.values()){
            q.update();
        }
    }

    public synchronized void loadOnlineLiveQueue(Queue queueInfo, List<QueueListLive> queue){
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
            onlineQueues.get(queue.getId()).leaveQueue(user);
            return true;
        }
        return false;
    }

    public synchronized List<QueueListLive> dataToSave(){
        return onlineQueues.values()
                .stream()
                .filter(a -> a.isUpdated() && a.isActive())
                .map(OnlineQueueLive::dataToSave)
                .flatMap(List::stream)
                .filter(a -> a != null)
                .collect(Collectors.toList());
    }
    public synchronized List<QueueListLive> dataToDelete(){
        return onlineQueues.values()
                .stream()
                .filter(a -> a.isUpdated() && a.isActive())
                .map(OnlineQueueLive::dataToDelete)
                .flatMap(List::stream)
                .filter(a -> a != null)
                .collect(Collectors.toList());
    }
    public synchronized List<Queue> queueInfoData(){
        return onlineQueues.values()
                .stream()
                .filter(a -> a.isUpdated() && a.isActive())
                .map(a -> a.getQueueInfo())
                .collect(Collectors.toList());
    }
    public synchronized int nextUser(Queue queue){
        if (!isOnline(queue))
            return -1;
        return onlineQueues.get(queue.getId()).nextUser();
    }

    public synchronized List<Queue> myOnlineQueues(User user){
        List<Queue> answ = new ArrayList<>();
        for (OnlineQueueLive q: onlineQueues.values()){
            if (q.isRegistered(user)) {
                int userBefore = q.usersBefore(user);
                if (userBefore >= 0) {
                    answ.add(new OnlineQueueData(q.getQueueInfo(), userBefore));
                }
            }
        }
        return answ;
    }
    public synchronized boolean skipAhead(Queue queue, User user){
        if (!isOnline(queue))
            return false;
        return onlineQueues.get(queue.getId()).skipAhead(user);
    }
}
