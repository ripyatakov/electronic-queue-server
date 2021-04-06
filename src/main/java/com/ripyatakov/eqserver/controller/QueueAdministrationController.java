package com.ripyatakov.eqserver.controller;

import com.ripyatakov.eqserver.entity.Queue;
import com.ripyatakov.eqserver.entity.User;
import com.ripyatakov.eqserver.json.QueueData;
import com.ripyatakov.eqserver.managers.OnlineQueuesManager;
import com.ripyatakov.eqserver.requests.AuthenticationRequest;
import com.ripyatakov.eqserver.requests.CreateQueueRequest;
import com.ripyatakov.eqserver.service.Hasher;
import com.ripyatakov.eqserver.service.OnlineQueueService;
import com.ripyatakov.eqserver.service.QueueService;
import com.ripyatakov.eqserver.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RestController
public class QueueAdministrationController {

    @Autowired
    private UserService userService;
    @Autowired
    private QueueService queueService;
    @Autowired
    private OnlineQueueService onlineQueueService;

    private boolean queueToOnline(Queue queue){
        return ((queue.getEqDateStart().getTime()/1000/60/60/24 == (new Date()).getTime()/1000/60/60/24) ||
        (queue.getEqDateStart().before(new Date()) && queue.getEqDateEnd().after(new Date())));
    }

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
            if (queueToOnline(queue)){
                onlineQueueService.loadOnlineLiveQueue(queue, new ArrayList<>());
                System.out.println("Queue online registered");
            }
            return ResponseEntity.status(HttpStatus.OK).body(new QueueData(queue, 0, Hasher.getQueueCode(queue.getId())));
        } catch (Exception exc){
            exc.printStackTrace();
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Can't create queue");
        }

    }
    @PostMapping("/createQueue")
    public ResponseEntity createQueue(@RequestBody CreateQueueRequest createQueueRequest){
        return this.createQueue(createQueueRequest, "live");

    }
    @PostMapping("/next/{qid}")
    public ResponseEntity next(@RequestBody AuthenticationRequest authenticationRequest, @PathVariable int qid) {
        try {
            User user = userService.getUserByToken(authenticationRequest.getToken());
            if (user == null)
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Somebody was authorized by your password");
            Queue queue = queueService.getQueueById(qid);
            if (queue == null)
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No such queue found");
            if (queue.getEqOwnerId() != user.getId()){
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("You are not an owner!");
            }
            int uid = onlineQueueService.nextUser(queue);
            if (uid == -1){
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No more users in queue or queue not started");
            } else{
                return ResponseEntity.status(200).body(userService.getUserById(uid));
            }
        } catch (Exception exc){
            exc.printStackTrace();
            return ResponseEntity.status(404).body("Something went wrong");
        }
    }


}
