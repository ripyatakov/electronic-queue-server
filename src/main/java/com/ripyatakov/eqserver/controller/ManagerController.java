package com.ripyatakov.eqserver.controller;

import com.ripyatakov.eqserver.entity.User;
import com.ripyatakov.eqserver.requests.AuthorizationRequest;
import com.ripyatakov.eqserver.requests.ManagerRequest;
import com.ripyatakov.eqserver.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Controller
public class ManagerController {
    @Autowired
    private UserService userService;
    @Autowired
    private QueueService queueService;
    @Autowired
    private OnlineQueueService onlineQueueService;

    @RequestMapping(value = "/mlogin", method = RequestMethod.GET)
    public String sender(Model model) {
        model.addAttribute("authorizationRequest", new AuthorizationRequest());
        return "mauthorization";
    }
    @PostMapping("/mlogin")
    public String greetingSubmit(@ModelAttribute AuthorizationRequest authorizationRequest, Model model) {
        List<User> userList = new ArrayList<>();
        User user = new User();
        try {
            user = userService.findByEmail(authorizationRequest.getEmail());
            authorizationRequest.setPassword(Hasher.sha256(authorizationRequest.getPassword()));
            if (user.getPassword().equals(authorizationRequest.getPassword())) {
                String token = TokenGenerator.getToken(authorizationRequest.getEmail(), authorizationRequest.getPassword());
                user.setToken(token);
                user = userService.updateUser(user);
            }
        }
        catch (Exception exc){
            exc.printStackTrace();
        }
        userList.add(user);
        model.addAttribute("userList", userList);
        model.addAttribute("managerRequest", new ManagerRequest());
        return "welcome";
    }
    @PostMapping("/mquery")
    public String findAllByName(@ModelAttribute ManagerRequest managerRequest, Model model) {
        List<User> userList = userService.findAllByName(managerRequest.getQuery());
        model.addAttribute("userList", userList);
        return "welcome";
    }
}
