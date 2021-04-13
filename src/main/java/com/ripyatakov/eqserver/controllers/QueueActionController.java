package com.ripyatakov.eqserver.controllers;

import com.ripyatakov.eqserver.entities.Queue;
import com.ripyatakov.eqserver.entities.QueueListLive;
import com.ripyatakov.eqserver.entities.Review;
import com.ripyatakov.eqserver.entities.User;
import com.ripyatakov.eqserver.json.QueueData;
import com.ripyatakov.eqserver.json.ResponseMessage;
import com.ripyatakov.eqserver.requests.ReviewRequest;
import com.ripyatakov.eqserver.requests.AuthenticationRequest;
import com.ripyatakov.eqserver.services.*;
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
        ResponseMessage responseMessage;
        try {
            User user = getUser(authenticationRequest);
            responseMessage = new ResponseMessage("Somebody was authorized by your password");
            if (user == null)
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseMessage);
            Queue queue = queueService.getQueueById(qid);
            responseMessage = new ResponseMessage("No such queue found");
            if (queue == null)
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseMessage);
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
                responseMessage = new ResponseMessage("Queue is full");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(responseMessage);
            }
        } catch (Exception exc) {
            exc.printStackTrace();
            responseMessage = new ResponseMessage("Something went wrong");
            return ResponseEntity.status(404).body(responseMessage);
        }
    }

    @PostMapping("/leaveQueue/{qid}")
    public ResponseEntity leaveQueue(@RequestBody AuthenticationRequest authenticationRequest, @PathVariable int qid) {
        ResponseMessage responseMessage;
        try {
            User user = getUser(authenticationRequest);
            responseMessage = new ResponseMessage("Somebody was authorized by your password");
            if (user == null)
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseMessage);
            Queue queue = queueService.getQueueById(qid);
            responseMessage = new ResponseMessage("No such queue found");
            if (queue == null)
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseMessage);
            responseMessage = new ResponseMessage("Successfully left queue");
            if (onlineQueueService.leaveQueue(queue, user)){
                return ResponseEntity.status(HttpStatus.OK).body(responseMessage);
            }
            responseMessage = new ResponseMessage("Successfully left queue");
            if (queueListLiveService.leaveQueue(queue, user))
                return ResponseEntity.status(HttpStatus.OK).body(responseMessage);
            else {
                responseMessage = new ResponseMessage("Already left");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseMessage);
            }
        } catch (Exception exc) {
            exc.printStackTrace();
            responseMessage = new ResponseMessage("Something went wrong");
            return ResponseEntity.status(404).body(responseMessage);
        }
    }

    @PostMapping("/myQueues")
    public ResponseEntity getMyQueues(@RequestBody AuthenticationRequest authenticationRequest) {
        ResponseMessage responseMessage;
        try {
            User user = getUser(authenticationRequest);
            responseMessage = new ResponseMessage("Somebody was authorized by your password");
            if (user == null)
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseMessage);
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
            responseMessage = new ResponseMessage("Something went wrong");
            return ResponseEntity.status(404).body(responseMessage);
        }
    }

    @PostMapping("/skipAhead/{qid}")
    public ResponseEntity skipAhead(@RequestBody AuthenticationRequest authenticationRequest, @PathVariable int qid) {
        ResponseMessage responseMessage;
        try {
            User user = getUser(authenticationRequest);
            responseMessage = new ResponseMessage("Somebody was authorized by your password");
            if (user == null)
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseMessage);
            Queue queue = queueService.getQueueById(qid);
            responseMessage = new ResponseMessage("No such queue found");
            if (queue == null)
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseMessage);
            responseMessage = new ResponseMessage("Successfully skipped");
            if (onlineQueueService.skipAhead(queue, user))
                return ResponseEntity.status(200).body(responseMessage);
            queueListLiveService.skipAhead(queue, user);
            responseMessage = new ResponseMessage("Successfully skipped");
            return ResponseEntity.status(200).body(responseMessage);
        } catch (Exception exc){
            exc.printStackTrace();
            responseMessage = new ResponseMessage("Something went wrong");
            return ResponseEntity.status(404).body(responseMessage);
        }
    }

    @GetMapping("/getQueue/{qid}")
    public ResponseEntity getQueue(@PathVariable int qid) {
        ResponseMessage responseMessage;
        try {
            Queue queue = queueService.getQueueById(qid);
            responseMessage = new ResponseMessage("No such queue found");
            if (queue == null)
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseMessage);

            return ResponseEntity.status(200).body(getQueueData(queue));
        } catch (Exception exc) {
            exc.printStackTrace();
            responseMessage = new ResponseMessage("Something went wrong");
            return ResponseEntity.status(404).body(responseMessage);
        }
    }
    @GetMapping("/getQueueByCode/{code}")
    public ResponseEntity getQueueByCode(@PathVariable String code) {
        return this.getQueue(Hasher.getQId(code));
    }

    @GetMapping("/getQueueSize/{qid}")
    public ResponseEntity getQueueSize(@PathVariable int qid) {
        ResponseMessage responseMessage;
        try {
            Queue queue = queueService.getQueueById(qid);
            responseMessage = new ResponseMessage("No such queue found");
            if (queue == null)
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseMessage);

            return ResponseEntity.status(200).body(queueListLiveService.queueSize(queue));
        } catch (Exception exc) {
            exc.printStackTrace();
            responseMessage = new ResponseMessage("Something went wrong");
            return ResponseEntity.status(404).body(responseMessage);
        }
    }
    @GetMapping("/todaysQueues")
    public ResponseEntity getTodaysQueues(){
        return ResponseEntity.status(200).body(queueService.todayQueues());
    }

    @PostMapping("/addReview/{qid}")
    public ResponseEntity addReview(@RequestBody ReviewRequest reviewRequest, @PathVariable int qid){
        ResponseMessage responseMessage;
        try {
            User user = getUser(reviewRequest);
            responseMessage = new ResponseMessage("Somebody was authorized by your password");
            if (user == null)
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseMessage);
            Queue queue = queueService.getQueueById(qid);
            responseMessage = new ResponseMessage("No such queue found");
            if (queue == null)
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseMessage);
            responseMessage = new ResponseMessage("It isn't your queue!");
            if (!queueListLiveService.isUserInQueue(queue, user)){
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseMessage);
            }
            responseMessage = new ResponseMessage("Successfully add review");
            reviewService.saveReview(new Review(qid, user.getId(), reviewRequest.getRate(), reviewRequest.getDescription(), new Date()));
            return ResponseEntity.status(200).body(responseMessage);
        } catch (Exception exc){
            exc.printStackTrace();
            responseMessage = new ResponseMessage("Something went wrong");
            return ResponseEntity.status(404).body(responseMessage);
        }
    }
}
