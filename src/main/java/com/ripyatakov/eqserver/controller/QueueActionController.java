package com.ripyatakov.eqserver.controller;

import com.ripyatakov.eqserver.entity.Queue;
import com.ripyatakov.eqserver.entity.QueueListLive;
import com.ripyatakov.eqserver.entity.Review;
import com.ripyatakov.eqserver.entity.User;
import com.ripyatakov.eqserver.json.QueueData;
import com.ripyatakov.eqserver.requests.ReviewRequest;
import com.ripyatakov.eqserver.requests.AuthenticationRequest;
import com.ripyatakov.eqserver.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class QueueActionController {
    @Autowired
    private UserService userService;
    @Autowired
    private QueueService queueService;
    @Autowired
    private QueueListLiveService queueListLiveService;
    @Autowired
    private OnlineQueueService onlineQueueService;
    @Autowired
    private ReviewService reviewService;

    private QueueData getQueueData(Queue queue){
        return new QueueData(queue, (int)queueListLiveService.queueSize(queue), Hasher.getQueueCode(queue.getId()));
    }

    private User getUser(AuthenticationRequest authenticationRequest) {
        return userService.getUserByToken(authenticationRequest.getToken());
    }

    @PostMapping("/registerForQueue/{qid}")
    public ResponseEntity registerForQueue(@RequestBody AuthenticationRequest authenticationRequest, @PathVariable int qid) {
        try {
            User user = getUser(authenticationRequest);
            if (user == null)
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Somebody was authorized by your password");
            Queue queue = queueService.getQueueById(qid);
            if (queue == null)
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No such queue found");
            QueueData queueData = onlineQueueService.registerForQueue(queue, user);
            if (queueData != null){
                return ResponseEntity.status(HttpStatus.OK).body(queueData);
            }
            queue = queueListLiveService.registerForQueue(queue, user);
            if (queue != null) {
                        queueData = new QueueData(queue,
                        queueListLiveService.usersBeforeMe(queue, user),
                        Hasher.getQueueCode(queue.getId())
                );
                return ResponseEntity.status(HttpStatus.OK).body(queueData);
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Queue is full");
            }
        } catch (Exception exc) {
            exc.printStackTrace();
            return ResponseEntity.status(404).body("Something went wrong");
        }
    }

    @PostMapping("/leaveQueue/{qid}")
    public ResponseEntity leaveQueue(@RequestBody AuthenticationRequest authenticationRequest, @PathVariable int qid) {
        try {
            User user = getUser(authenticationRequest);
            if (user == null)
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Somebody was authorized by your password");
            Queue queue = queueService.getQueueById(qid);
            if (queue == null)
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No such queue found");
            if (onlineQueueService.leaveQueue(queue, user)){
                return ResponseEntity.status(HttpStatus.OK).body("Successfully left queue");
            }
            if (queueListLiveService.leaveQueue(queue, user))
                return ResponseEntity.status(HttpStatus.OK).body("Successfully left queue");
            else
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Already left");
        } catch (Exception exc) {
            exc.printStackTrace();
            return ResponseEntity.status(404).body("Something went wrong");
        }
    }

    @PostMapping("/myQueues")
    public ResponseEntity getMyQueues(@RequestBody AuthenticationRequest authenticationRequest) {
        try {
            User user = getUser(authenticationRequest);
            if (user == null)
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Somebody was authorized by your password");
            List<QueueListLive> queueListLives = queueListLiveService.getMyQueueList(user);
            queueListLives = queueListLives
                    .stream()
                    .filter(
                        a -> !onlineQueueService.isOnline(a.getEqQId())
                    )
                    .collect(Collectors.toList());
            List<Queue> myQueues = queueService.getQueuesFromList(queueListLives);
            myQueues.addAll(onlineQueueService.myOnlineQueues(user));
            List<QueueData> queueDataList = myQueues.stream()
                    .map(a -> new QueueData(a, queueListLiveService.usersBeforeMe(a, user), Hasher.getQueueCode(a.getId())))
                    .collect(Collectors.toList());
            return ResponseEntity.status(200).body(queueDataList);
        } catch (Exception exc) {
            exc.printStackTrace();
            return ResponseEntity.status(404).body("Something went wrong");
        }
    }

    @PostMapping("/skipAhead/{qid}")
    public ResponseEntity skipAhead(@RequestBody AuthenticationRequest authenticationRequest, @PathVariable int qid) {
        try {
            User user = getUser(authenticationRequest);
            if (user == null)
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Somebody was authorized by your password");
            Queue queue = queueService.getQueueById(qid);
            if (queue == null)
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No such queue found");
            if (onlineQueueService.skipAhead(queue, user))
                return ResponseEntity.status(200).body("Successfully skipped");
            queueListLiveService.skipAhead(queue, user);
            return ResponseEntity.status(200).body("Successfully skipped");
        } catch (Exception exc){
            exc.printStackTrace();
            return ResponseEntity.status(404).body("You are the last at queue");
        }
    }

    @GetMapping("/getQueue/{qid}")
    public ResponseEntity getQueue(@PathVariable int qid) {
        try {
            Queue queue = queueService.getQueueById(qid);
            if (queue == null)
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No such queue found");

            return ResponseEntity.status(200).body(getQueueData(queue));
        } catch (Exception exc) {
            exc.printStackTrace();
            return ResponseEntity.status(404).body("Something went wrong");
        }
    }
    @GetMapping("/getQueueByCode/{code}")
    public ResponseEntity getQueueByCode(@PathVariable String code) {
        return this.getQueue(Hasher.getQId(code));
    }

    @GetMapping("/getQueueSize/{qid}")
    public ResponseEntity getQueueSize(@PathVariable int qid) {
        try {
            Queue queue = queueService.getQueueById(qid);
            if (queue == null)
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No such queue found");

            return ResponseEntity.status(200).body(queueListLiveService.queueSize(queue));
        } catch (Exception exc) {
            exc.printStackTrace();
            return ResponseEntity.status(404).body("Something went wrong");
        }
    }
    @GetMapping("/todaysQueues")
    public ResponseEntity getTodaysQueues(){
        return ResponseEntity.status(200).body(queueService.todayQueues());
    }

    @PostMapping("/addReview/{qid}")
    public ResponseEntity addReview(@RequestBody ReviewRequest reviewRequest, @PathVariable int qid){
        try {
            User user = getUser(reviewRequest);
            if (user == null)
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Somebody was authorized by your password");
            Queue queue = queueService.getQueueById(qid);
            if (queue == null)
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No such queue found");
            if (!queueListLiveService.isUserInQueue(queue, user)){
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("It isn't your queue!");
            }
            reviewService.saveReview(new Review(qid, user.getId(), reviewRequest.getRate(), reviewRequest.getDescription(), new Date()));
            return ResponseEntity.status(200).body("Successfully add review");
        } catch (Exception exc){
            exc.printStackTrace();
            return ResponseEntity.status(404).body("Something went wrong");
        }
    }
}
