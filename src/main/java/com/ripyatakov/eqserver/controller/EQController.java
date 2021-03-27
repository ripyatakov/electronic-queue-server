package com.ripyatakov.eqserver.controller;

import com.ripyatakov.eqserver.entity.User;
import com.ripyatakov.eqserver.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class EQController {

    @Autowired
    private UserService service;

    @PostMapping("/addUser")
    public User addUser(@RequestBody User user) {
        return service.saveUsers(user);
    }
    @PostMapping("/addUsers")
    public List<User> addUsers(@RequestBody List<User> users){
        return service.saveUsers(users);
    }
    @GetMapping("/getUsers")
    public List<User> findAllUsers(){
        return service.getAllUsers();
    }
    @GetMapping("/getUsers/{id}")
    public User findUserById(@PathVariable int id){
        return service.getUserById(id);
    }
    @GetMapping("/getUsers/{name}")
    public List<User> findUserByName(@PathVariable String name){
        return service.getUserByName(name);
    }

}
