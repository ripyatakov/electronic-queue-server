package com.ripyatakov.eqserver.pdo;

import com.ripyatakov.eqserver.entity.Queue;
import com.ripyatakov.eqserver.entity.User;

import java.util.Date;

public interface OnlineQueue {

    int isRegistered(User user);

    boolean registerForQueue(User user);

    boolean leaveQueue(User user);

    boolean isUpdated();

    boolean isActive();

    float getAverageWaitingTime();

    int nextUser();

    int previousUser();

    int currentUser();

    Date getStartDate();

    Date getEndDate();

    void update();


}
