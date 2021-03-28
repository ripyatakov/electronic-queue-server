package com.ripyatakov.eqserver.controller;

import com.ripyatakov.eqserver.entity.Queue;
import com.ripyatakov.eqserver.entity.User;
import com.ripyatakov.eqserver.requests.CreateQueueRequest;
import com.ripyatakov.eqserver.service.QueueService;
import com.ripyatakov.eqserver.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class QueueAdministrationController {

    @Autowired
    private UserService userService;
    @Autowired
    private QueueService queueService;

    @GetMapping("/allQueues")
    public List<Queue> allQueues(){
        return queueService.getAllQueues();
    }

    @PostMapping("/createQueue/{type}")
    public ResponseEntity createQueue(@RequestBody CreateQueueRequest createQueueRequest, @PathVariable String type){
        try {
            User owner = userService.getUserByToken(createQueueRequest.getToken());
            Queue queue = new Queue(0,
                    owner.getId(),
                    createQueueRequest.getMaxUsers(),
                    0,
                    createQueueRequest.getDateStart(),
                    createQueueRequest.getDateEnd(),
                    type,
                    createQueueRequest.getMaxUsers(),
                    createQueueRequest.getTitle(),
                    createQueueRequest.getDescription()
                    );
            queue = queueService.saveQueue(queue);
            return ResponseEntity.status(HttpStatus.OK).body(queue);
        } catch (Exception exc){
            exc.printStackTrace();
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Can't create queue");
        }

    }
    @PostMapping("/createQueue")
    public ResponseEntity createQueue(@RequestBody CreateQueueRequest createQueueRequest){
        try {
            User owner = userService.getUserByToken(createQueueRequest.getToken());
            Queue queue = new Queue(0,
                    owner.getId(),
                    createQueueRequest.getMaxUsers(),
                    0,
                    createQueueRequest.getDateStart(),
                    createQueueRequest.getDateEnd(),
                    "live",
                    createQueueRequest.getMaxUsers(),
                    createQueueRequest.getTitle(),
                    createQueueRequest.getDescription()
            );
            queue = queueService.saveQueue(queue);
            return ResponseEntity.status(HttpStatus.OK).body(queue);
        } catch (Exception exc){
            exc.printStackTrace();
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Can't create queue");
        }

    }



}
