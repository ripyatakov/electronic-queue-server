package com.ripyatakov.eqserver.managers;

import com.ripyatakov.eqserver.entity.Queue;
import com.ripyatakov.eqserver.service.OnlineQueueService;
import com.ripyatakov.eqserver.service.QueueListLiveService;
import com.ripyatakov.eqserver.service.QueueService;
import com.ripyatakov.eqserver.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;

@Service
@RequiredArgsConstructor
@EnableScheduling
public class OnlineQueuesManager {
    @Autowired
    private UserService userService;
    @Autowired
    private QueueService queueService;
    @Autowired
    private QueueListLiveService queueListLiveService;
    @Autowired
    private final OnlineQueueService onlineQueuesService;

    @PostConstruct
    void initializeOnlineQueues(){
        List<Queue> todayQueues = queueService.todayQueues();
        System.out.println("Add " +  todayQueues.size() + " online queues");
        for (Queue q: todayQueues) {
            onlineQueuesService.loadOnlineLiveQueue(q, queueListLiveService.getQueueRecordings(q));
        }
        List<Queue> activeQueues = queueService.findByStatus("active");
        for (Queue q: activeQueues) {
            if (!onlineQueuesService.isOnline(q)){
                q.setEqStatus("inactive");
                queueService.saveQueue(q);
            }
        }
    }

    @Scheduled(fixedDelayString = "${queueUpdateDelay}", initialDelayString = "${queueInitialDelay}"  )
    private synchronized void updateQueues(){
        System.out.println("Update beginning");
        System.out.println("Delete: \t" + queueListLiveService.deleteAll(onlineQueuesService.dataToDelete()) + " records");
        System.out.println("Save: \t" + queueListLiveService.saveAll(onlineQueuesService.dataToSave()) + " records");
        System.out.println("Save queueInfos: \t" + queueService.saveQueues(onlineQueuesService.queueInfoData()) + " records");
        onlineQueuesService.clearDeletedRecords();
        onlineQueuesService.clearInactiveQueues();
        List<Queue> queues = onlineQueuesService.removeOfflineQueues();
        if (queues != null && queues.size() > 0) {
            for (Queue q : queues) {
                q.setEqStatus("End");
            }
            queueService.saveQueues(queues);
        }
    }
    @Scheduled(cron = "0/42 0 00 * * *")
    private synchronized void reloadingQueues(){
        System.out.println("Start queue reloading");
        updateQueues();
        initializeOnlineQueues();
        System.out.println("Reloading successfully");
    }
}
