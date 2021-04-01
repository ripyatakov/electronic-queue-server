package com.ripyatakov.eqserver.pdo;

import com.ripyatakov.eqserver.entity.Queue;
import com.ripyatakov.eqserver.entity.QueueListLive;
import com.ripyatakov.eqserver.entity.User;

import java.util.*;

public class OnlineQueueLive implements OnlineQueue {

    public OnlineQueueLive(Queue queueInfo, List<QueueListLive> queue) {
        this.queueInfo = queueInfo;
        //this.currentUserIndex = queueInfo.getEqCurrentUser();
        this.queue = Collections.synchronizedList(new ArrayList<>(queue));
        this.queue.sort(new Comparator<QueueListLive>() {
            @Override
            public int compare(QueueListLive o1, QueueListLive o2) {
                return o1.compareTo(o2);
            }
        }
        );
        this.toDelete = Collections.synchronizedList(new ArrayList<>());
    }

    public Queue getQueueInfo() {
        return queueInfo;
    }

    Queue queueInfo;
    //int currentUserIndex = 0;
    final List<QueueListLive> queue;
    boolean updated = false;

    List<QueueListLive> toDelete;

    @Override
    public synchronized int isRegistered(User user) {
        for (int i = 0; i < queue.size(); i++) {
            if (queue.get(i).getEqUId() == user.getId()){
                return i - queueInfo.getEqCurrentUser();
            }
        }
        return -1;
    }

    @Override
    public boolean registerForQueue(User user) {
        try {
            synchronized (queue) {
                QueueListLive newRecord = new QueueListLive(queueInfo.getId(), user.getId(), getNewEqNumber());
                if (queue.size() >= queueInfo.getEqMaxUsers() || queue.contains(newRecord))
                    return false;
                queue.add(newRecord);
                updated = true;
                return true;
            }
        } catch (Exception exc){
            exc.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean leaveQueue(User user) {
        try {
            synchronized (queue) {
                int userIndex = -1;
                for (int i = 0; i < queue.size(); i++) {
                    if (queue.get(i).getEqUId() == user.getId()) {
                        userIndex = i;
                    }
                }
                if (userIndex == -1 || userIndex >= queue.size()) {
                    return false;
                }
                if (userIndex <= queueInfo.getEqCurrentUser())
                    return false;

                synchronized (toDelete){
                    toDelete.add(queue.remove(userIndex));
                }
                updated = true;
            }
            return true;
        } catch (Exception exc) {
            exc.printStackTrace();
            return false;
        }

    }

    @Override
    public boolean isUpdated() {
        return updated;
    }

    @Override
    public boolean isActive() {
        return true;
    }

    @Override
    public float getAverageWaitingTime() {
        return 0;
    }

    @Override
    public synchronized int nextUser() {
        int cui = queueInfo.getEqCurrentUser() + 1;
        if (cui < 0 || cui >= queue.size())
            return -1;
        queueInfo.setEqCurrentUser(cui);
        synchronized (queue) {
            updated = true;
            return queue.get(queueInfo.getEqCurrentUser()).getEqUId();
        }
    }


    @Override
    public int previousUser() {
        int cui = queueInfo.getEqCurrentUser() - 1;
        if (cui < 0 || cui >= queue.size())
            return -1;
        return queue.get(cui).getEqUId();
    }

    @Override
    public int currentUser() {
        if (queueInfo.getEqCurrentUser() < 0 || queueInfo.getEqCurrentUser() >= queue.size())
            return -1;
        return queue.get(queueInfo.getEqCurrentUser()).getEqUId();
    }

    @Override
    public Date getStartDate() {
        return queueInfo.getEqDateStart();
    }

    @Override
    public Date getEndDate() {
        return queueInfo.getEqDateEnd();
    }

    @Override
    public void update(){
        toDelete.clear();
        toDelete = Collections.synchronizedList(new ArrayList<>());
        updated = false;
    }

    private int getNewEqNumber() {
        synchronized (queue) {
            if (queue.isEmpty())
                return 0;
            return Collections.max(queue).getEqNumber() + 1;
        }
    }

    public List<QueueListLive> dataToSave(){
        return queue;
    }
    public List<QueueListLive> dataToDelete() {
        return toDelete;
    }


}
