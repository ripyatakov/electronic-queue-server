package com.ripyatakov.eqserver.pdo;

import com.ripyatakov.eqserver.entity.Queue;
import com.ripyatakov.eqserver.entity.QueueListLive;
import com.ripyatakov.eqserver.entity.User;
import com.ripyatakov.eqserver.json.QueueData;
import com.ripyatakov.eqserver.service.Hasher;

import java.util.*;

public class OnlineQueueLive implements OnlineQueue {

    private final int minutesBeforeDelete = 10;
    public OnlineQueueLive(Queue queueInfo, List<QueueListLive> queue) {
        this.queueInfo = queueInfo;
        isBreak = false;
        updated = true;
        queueInfo.setEqStatus("active");
        //this.currentUserIndex = queueInfo.getEqCurrentUser();
        this.queue = Collections.synchronizedList(new ArrayList<>(queue));
        this.lastVisit = new Date();
        this.queue.sort(new Comparator<QueueListLive>() {
            @Override
            public int compare(QueueListLive o1, QueueListLive o2) {
                return o1.compareTo(o2);
            }
        }
        );
        if (queueInfo.getEqCurrentUser() < 0){
            updated = true;
            queueInfo.setEqCurrentUser(0);
        }
        if (queueInfo.getEqCurrentUser() > queue.size()){
            queueInfo.setEqCurrentUser(queue.size());
            updated = true;
        }
        this.toDelete = Collections.synchronizedList(new ArrayList<>());
    }

    public Queue getQueueInfo() {
        return queueInfo;
    }
    Date lastVisit;
    Queue queueInfo;
    //int currentUserIndex = 0;
    final List<QueueListLive> queue;
    boolean updated = false;
    boolean isBreak;
    List<QueueListLive> toDelete;

    /**
     * returns count users before user
     * @param user
     * @return int [-..+]
     */
    @Override
    public synchronized boolean isRegistered(User user) {
        synchronized (queue) {
            for (int i = 0; i < queue.size(); i++) {
                if (queue.get(i).getEqUId() == user.getId()) {
                    return true;
                }
            }
        }
        return false;
    }
    @Override
    public synchronized int usersBefore(User user) {
        synchronized (queue) {
            for (int i = 0; i < queue.size(); i++) {
                if (queue.get(i).getEqUId() == user.getId()) {
                    return i - queueInfo.getEqCurrentUser();
                }
            }
        }
        return -1;
    }

    @Override
    public synchronized QueueData registerForQueue(User user) {
        try {
            synchronized (queue) {
                QueueListLive newRecord = new QueueListLive(queueInfo.getId(), user.getId(), getNewEqNumber());
                if (queue.size() >= queueInfo.getEqMaxUsers() || queue.contains(newRecord))
                    return null;
                queue.add(newRecord);
                updated = true;
                if (isBreak){
                    lastVisit = new Date();
                }
                isBreak = false;
                return new QueueData(queueInfo, usersBefore(user), Hasher.getQueueCode(queueInfo.getId()));
            }
        } catch (Exception exc){
            exc.printStackTrace();
            return null;
        }
    }

    @Override
    public synchronized boolean leaveQueue(User user) {
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
    public synchronized boolean isUpdated() {
        return updated;
    }

    @Override
    public synchronized boolean isActive() {
        return (new Date().getTime() - queueInfo.getEqDateEnd().getTime()) / 1000 / 60 < minutesBeforeDelete;
    }

    @Override
    public synchronized float getAverageWaitingTime() {
        return 0;
    }

    /**
     * cui can be queue.size() it means that queue is empty but ready for serve new user
     * @return
     */
    @Override
    public synchronized int nextUser() {
        int cui = queueInfo.getEqCurrentUser() + 1;
        if (cui < 0 || cui > queue.size())
            return -1;
        queueInfo.setEqCurrentUser(cui);

        long secondsBetweenVisits = ((new Date()).getTime() - lastVisit.getTime())/1000;
        queueInfo.setEqAverageWaitingTime((queueInfo.getEqAverageWaitingTime()*(queueInfo.getEqCurrentUser()+1) +
                secondsBetweenVisits/60.0)/((queueInfo.getEqCurrentUser()+2)));

        if (queueInfo.getEqCurrentUser() == queue.size()) {
            isBreak = true;
            return -2;
        }
        synchronized (queue) {
            updated = true;
            return queue.get(queueInfo.getEqCurrentUser()).getEqUId();
        }
    }


    @Override
    public synchronized int previousUser() {
        int cui = queueInfo.getEqCurrentUser() - 1;
        if (cui < 0 || cui >= queue.size())
            return -1;
        return queue.get(cui).getEqUId();
    }

    @Override
    public synchronized int currentUser() {
        if (queueInfo.getEqCurrentUser() < 0 || queueInfo.getEqCurrentUser() >= queue.size())
            return -1;
        return queue.get(queueInfo.getEqCurrentUser()).getEqUId();
    }

    @Override
    public synchronized Date getStartDate() {
        return queueInfo.getEqDateStart();
    }

    @Override
    public synchronized Date getEndDate() {
        return queueInfo.getEqDateEnd();
    }

    @Override
    public synchronized void update(){
        synchronized (toDelete) {
            toDelete.clear();
            toDelete = Collections.synchronizedList(new ArrayList<>());
            updated = false;
        }
    }

    private synchronized int getNewEqNumber() {
        synchronized (queue) {
            if (queue.isEmpty())
                return 0;
            return queue.get(queue.size()-1).getEqNumber() + 1;
        }
    }

    public synchronized List<QueueListLive> dataToSave(){
        return queue;
    }
    public synchronized List<QueueListLive> dataToDelete() {
        return toDelete;
    }

    public synchronized boolean skipAhead(User user){
        int userIndex = -1;
        synchronized (queue) {
            for (int i = 0; i < queue.size(); i++) {
                if (queue.get(i).getEqUId() == user.getId()) {
                    userIndex = i;
                }
            }
            if (userIndex == -1 || userIndex >= queue.size() - 1) {
                return false;
            }
            try {
                QueueListLive t = queue.get(userIndex);

                queue.set(userIndex, queue.get(userIndex + 1));
                queue.set(userIndex + 1, t);
                int intt = queue.get(userIndex).getEqNumber();
                queue.get(userIndex).setEqNumber(queue.get(userIndex + 1).getEqNumber());
                queue.get(userIndex + 1).setEqNumber(intt);
                updated = true;

                return true;
            } catch (Exception exc){
                return false;
            }
        }
    }


}
