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
public class EQController {

    @Autowired
    private UserService userService;
    @Autowired
    private QueueService queueService;

    @PostMapping("/addUser")
    public User addUser(@RequestBody User user) {
        return userService.saveUser(user);
    }
    @PostMapping("/addUsers")
    public List<User> addUsers(@RequestBody List<User> users){
        return userService.saveUsers(users);
    }
    @GetMapping("/getUsers")
    public List<User> findAllUsers(){
        return userService.getAllUsers();
    }
    @GetMapping("/getUsers/{id}")
    public User findUserById(@PathVariable int id){
        return userService.getUserById(id);
    }
    @GetMapping("/getUsers/{name}")
    public List<User> findUserByName(@PathVariable String name){
        return userService.getUserByName(name);
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
            return ResponseEntity.status(HttpStatus.OK).body(queue);
        } catch (Exception exc){
            exc.printStackTrace();
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Can't create queue");
        }

    }




}
