package com.ripyatakov.eqserver.controller;

import com.ripyatakov.eqserver.entity.Queue;
import com.ripyatakov.eqserver.entity.User;
import com.ripyatakov.eqserver.json.QueueData;
import com.ripyatakov.eqserver.json.ResponseMessage;
import com.ripyatakov.eqserver.managers.OnlineQueuesManager;
import com.ripyatakov.eqserver.requests.AuthenticationRequest;
import com.ripyatakov.eqserver.requests.CreateQueueRequest;
import com.ripyatakov.eqserver.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class QueueAdministrationController {

    @Autowired
    private UserService userService;
    @Autowired
    private QueueService queueService;
    @Autowired
    private QueueListLiveService queueListLiveService;
    @Autowired
    private OnlineQueueService onlineQueueService;

    private List<QueueData> getQueueDatas(List<Queue> queues){
        return
                queues.stream()
                .map(a -> new QueueData(a,(int)queueListLiveService.queueSize(a), Hasher.getQueueCode(a.getId()) ))
                .collect(Collectors.toList());
    }

    private QueueData getQueueData(Queue queue){
        return new QueueData(queue, (int)queueListLiveService.queueSize(queue), Hasher.getQueueCode(queue.getId()));
    }

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
        ResponseMessage responseMessage;
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
                    createQueueRequest.getDescription(),
                    "inactive"
                    );
            queue = queueService.saveQueue(queue);
            if (queueToOnline(queue)){
                onlineQueueService.loadOnlineLiveQueue(queue, new ArrayList<>());
                System.out.println("Queue online registered");
            }
            return ResponseEntity.status(HttpStatus.OK).body(new QueueData(queue, 0, Hasher.getQueueCode(queue.getId())));
        } catch (Exception exc){
            exc.printStackTrace();
            responseMessage = new ResponseMessage("Can't create queue");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(responseMessage);
        }

    }
    @PostMapping("/createQueue")
    public ResponseEntity createQueue(@RequestBody CreateQueueRequest createQueueRequest){
        return this.createQueue(createQueueRequest, "live");

    }
    @PostMapping("/next/{qid}")
    public ResponseEntity next(@RequestBody AuthenticationRequest authenticationRequest, @PathVariable int qid) {
        ResponseMessage responseMessage;
        try {
            User user = userService.getUserByToken(authenticationRequest.getToken());
            responseMessage = new ResponseMessage("Somebody was authorized by your password");
            if (user == null)
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseMessage);
            Queue queue = queueService.getQueueById(qid);
            responseMessage = new ResponseMessage("No such queue found");
            if (queue == null)
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseMessage);
            responseMessage = new ResponseMessage("You are not an owner!");
            if (queue.getEqOwnerId() != user.getId()){
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseMessage);
            }
            int uid = onlineQueueService.nextUser(queue);
            responseMessage = new ResponseMessage("No more users in queue or queue not started");
            if (uid == -1){
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseMessage);
            } else{
                return ResponseEntity.status(200).body(userService.getUserById(uid));
            }
        } catch (Exception exc){
            exc.printStackTrace();
            responseMessage = new ResponseMessage("No more users in queue or queue not started");
            return ResponseEntity.status(404).body(responseMessage);
        }
    }

    @PostMapping("/myAdminsQueues")
    public ResponseEntity myAdminsQueues(@RequestBody AuthenticationRequest authenticationRequest) {
        ResponseMessage responseMessage;
        try {
            User user = userService.getUserByToken(authenticationRequest.getToken());
            responseMessage = new ResponseMessage("Somebody was authorized by your password");
            if (user == null)
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseMessage);
            return ResponseEntity.status(200).body(getQueueDatas(queueService.getQueuesByOwnerId(user.getId())));
        } catch (Exception exc){
            exc.printStackTrace();
            responseMessage = new ResponseMessage("No more users in queue or queue not started");
            return ResponseEntity.status(404).body(responseMessage);
        }
    }
}
