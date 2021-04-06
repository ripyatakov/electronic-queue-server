package com.ripyatakov.eqserver.json;

import com.ripyatakov.eqserver.entity.Queue;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QueueData extends Queue {
    public QueueData(Queue q, int usersBeforeMe, String code){
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
        this.code = code;
    }
    private int usersBeforeMe;
    private double expectedTime;
    private String code;
}
