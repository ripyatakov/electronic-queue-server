package com.ripyatakov.eqserver.pdo;

import com.ripyatakov.eqserver.entity.Queue;
import com.ripyatakov.eqserver.entity.User;

import java.util.Date;

public interface OnlineQueue {

    void registerForQueue(User user);

    void leaveQueue(Queue queue);

    boolean isUpdated();

    boolean isActive();

    float getAverageWaitingTime();

    User nextUser();

    User previousUser();

    Date getStartDate();

    Date getEndDate();


}
