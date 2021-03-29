package com.ripyatakov.eqserver.schedulers;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@EnableScheduling
public class ActiveQueueScheduler {
    @Scheduled(fixedDelayString = "${queueUpdateDelay}", initialDelayString = "${queueInitialDelay}"  )
    private void updateQueues(){
        System.out.println("Updated");
    }
}
