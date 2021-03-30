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
        for (Queue q: todayQueues) {
            onlineQueuesService.loadOnlineLiveQueue(q, queueListLiveService.getQueueRecordings(q));
        }
    }
    @Scheduled(fixedDelayString = "${queueUpdateDelay}", initialDelayString = "${queueInitialDelay}"  )
    private void updateQueues(){
        System.out.println("Updated");
    }
}
