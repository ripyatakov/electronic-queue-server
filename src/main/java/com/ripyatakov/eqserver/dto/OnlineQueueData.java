package com.ripyatakov.eqserver.dto;

import com.ripyatakov.eqserver.entity.Queue;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OnlineQueueData extends Queue {
    public OnlineQueueData(Queue q, int usersBeforeMe){
        this.setId(q.getId());
        this.setEqOwnerId(q.getEqOwnerId());
        this.setEqAverageWaitingTime(q.getEqAverageWaitingTime());
        this.setEqCurrentUser(q.getEqCurrentUser());
        this.setEqDateEnd(q.getEqDateEnd());
        this.setEqDateStart(q.getEqDateStart());
        this.setEqType(q.getEqType());
        this.setEqMaxUsers(q.getEqMaxUsers());
        this.setEqTitle(q.getEqTitle());
        this.setEqDescription(q.getEqDescription());
        this.usersBeforeMe = usersBeforeMe;
        this.expectedTime = usersBeforeMe*q.getEqAverageWaitingTime();
    }
    private int usersBeforeMe;
    private double expectedTime;
}
