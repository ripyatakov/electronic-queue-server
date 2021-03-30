package com.ripyatakov.eqserver.pdo;

import com.ripyatakov.eqserver.entity.Queue;
import com.ripyatakov.eqserver.entity.QueueListLive;
import com.ripyatakov.eqserver.entity.User;

import java.util.*;

public class OnlineQueueLive implements OnlineQueue {

    public OnlineQueueLive(Queue queueInfo, List<QueueListLive> queue) {
        this.queueInfo = queueInfo;
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

    Queue queueInfo;
    int currentUserIndex = 0;
    List<QueueListLive> queue;
    boolean updated = false;

    List<QueueListLive> toDelete;

    @Override
    public boolean registerForQueue(User user) {
        try {
            synchronized (queue) {
                if (queue.size() >= queueInfo.getEqMaxUsers())
                    return false;
                queue.add(new QueueListLive(queueInfo.getId(), user.getId(), getNewEqNumber()));
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
                if (userIndex < currentUserIndex)
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
    public int nextUser() {
        int cui = currentUserIndex + 1;
        if (cui < 0 || cui >= queue.size())
            return -1;
        return queue.get(cui).getEqUId();
    }

    @Override
    public int previousUser() {
        int cui = currentUserIndex - 1;
        if (cui < 0 || cui >= queue.size())
            return -1;
        return queue.get(cui).getEqUId();
    }

    @Override
    public int currentUser() {
        if (currentUserIndex < 0 || currentUserIndex >= queue.size())
            return -1;
        return queue.get(currentUserIndex).getEqUId();
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
        toDelete = Collections.synchronizedList(new ArrayList<>());
        updated = false;
    }

    private int getNewEqNumber() {
        if (queue.isEmpty())
            return 0;
        return Collections.max(queue).getEqNumber() + 1;
    }

    public List<QueueListLive> dataToSave(){
        return queue;
    }
    public List<QueueListLive> dataToDelete() {
        return toDelete;
    }

}
