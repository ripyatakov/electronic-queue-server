package com.ripyatakov.eqserver.service;

import com.ripyatakov.eqserver.entity.Queue;
import com.ripyatakov.eqserver.entity.QueueListLive;
import com.ripyatakov.eqserver.entity.User;
import com.ripyatakov.eqserver.repository.QueueListLiveRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
public class QueueListLiveService {
    @Autowired
    QueueListLiveRepository repository;

    public QueueListLive save(QueueListLive queueListLive){
        return repository.save(queueListLive);
    }

    public int deleteAll(List<QueueListLive> queues){
        repository.deleteAll(queues);
        return queues.size();
    }

    public int saveAll(List<QueueListLive> queues){
        return repository.saveAll(queues).size();
    }

    public Queue registerForQueue(Queue queue, User user){
        if (repository.countByEqQId(queue.getId()) < queue.getEqMaxUsers()){
            QueueListLive q = repository.findFirstByEqQIdOrderByEqNumberDesc(queue.getId());
            int eqNumber = 0;
            if (q != null)
                eqNumber = q.getEqNumber() + 1;
            QueueListLive newRecord = new QueueListLive(queue.getId(), user.getId(),eqNumber, queue.getEqDateStart(), 0, new Date());
            repository.save(newRecord);
            return queue;
        }
        return null;
    }

    public boolean skipAhead(Queue queue, User user){
        QueueListLive user1 = repository.findByEqQIdAndEqUId(queue.getId(), user.getId());
        int a = user1.getEqNumber();
        QueueListLive user2 = repository.findFirstByEqNumberGreaterThanAndEqQIdOrderByEqNumber(user1.getEqNumber(), user1.getEqQId());
        int b = user2.getEqNumber();
        user1.setEqNumber(b);
        user2.setEqNumber(a);
        repository.save(user1);
        repository.save(user2);
        return true;
    }

    public long queueSize(Queue queue){
        return repository.countByEqQId(queue.getId());
    }

    public boolean leaveQueue(Queue queue, User user){

        QueueListLive queueList = repository.findByEqQIdAndEqUId(queue.getId(), user.getId());
        if (queueList == null)
            return false;
        repository.delete(queueList);
        return true;
    }

    public List<QueueListLive> getMyQueueList(User user){
        return repository.findByEqUId(user.getId());
    }

    public List<QueueListLive> getQueueRecordings(Queue queue){
        return repository.findByEqQId(queue.getId());
    }
    public List<QueueListLive> getQueueRecordings(int qid){
        return repository.findByEqQId(qid);
    }
    public int usersBeforeMe(Queue queue, User user){
        QueueListLive user1 = repository.findByEqQIdAndEqUId(queue.getId(), user.getId());
        return (int) repository.countByEqNumberLessThanAndEqQId(user1.getEqNumber(), queue.getId());
    }

    public boolean isUserInQueue(Queue queue, User user){
        QueueListLive queueListLive = repository.findByEqQIdAndEqUId(queue.getId(), user.getId());
        return queueListLive != null;
    }
    public List<Integer> usersRegistersByHours(int qid){
        List<Integer> lst = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(repository.findByEqQId(qid).get(0).getEqEnterTime());
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        Date d1 = calendar.getTime();
        Date d2 = new Date(d1.getTime() + 60*60*1000);
        for (int i = 0; i < 24; i++) {
            lst.add((int)repository.countByEqEnterTimeBetweenAndEqQId(d1,d2,qid));
            d1 = d2;
            d2 = new Date(d1.getTime() + 60*60*1000);
        }
        return lst;
    }
}
