package com.ripyatakov.eqserver.pdo;

import com.ripyatakov.eqserver.entities.User;
import com.ripyatakov.eqserver.json.QueueData;

import java.util.Date;

public interface OnlineQueue {

    boolean isRegistered(User user);

    int usersBefore(User user);

    QueueData registerForQueue(User user);

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
