package com.ripyatakov.eqserver.controller;

import com.ripyatakov.eqserver.entity.Queue;
import com.ripyatakov.eqserver.entity.QueueListLive;
import com.ripyatakov.eqserver.entity.User;
import com.ripyatakov.eqserver.requests.AuthenticationRequest;
import com.ripyatakov.eqserver.requests.AuthorizationRequest;
import com.ripyatakov.eqserver.requests.CreateQueueRequest;
import com.ripyatakov.eqserver.service.QueueListLiveService;
import com.ripyatakov.eqserver.service.QueueService;
import com.ripyatakov.eqserver.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class QueueRegistrationController {
    @Autowired
    private UserService userService;
    @Autowired
    private QueueService queueService;
    @Autowired
    private QueueListLiveService queueListLiveService;

    private User getUser(AuthenticationRequest authenticationRequest){
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
            if (queueListLiveService.registerForQueue(queue,user)){
                return ResponseEntity.status(HttpStatus.OK).body("Successfully registered");
            } else{
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Queue is full");
            }
        } catch (Exception exc){
            exc.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Something went wrong");
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
            if (queueListLiveService.leaveQueue(queue, user))
                return ResponseEntity.status(HttpStatus.OK).body("Successfully left queue");
            else
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Already left");
        } catch (Exception exc){
            exc.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Something went wrong");
        }
    }

    @PostMapping("/myQueues")
    public ResponseEntity getMyQueues(@RequestBody AuthenticationRequest authenticationRequest){
        try {
            User user = getUser(authenticationRequest);
            if (user == null)
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Somebody was authorized by your password");
            List<QueueListLive> queueListLives = queueListLiveService.getMyQueueList(user);
            List<Queue> myQueues = queueService.getQueuesFromList(queueListLives);
            return ResponseEntity.status(200).body(myQueues);
        } catch (Exception exc){
            exc.printStackTrace();
            return ResponseEntity.status(404).body("Something went wrong");
        }
    }
}
