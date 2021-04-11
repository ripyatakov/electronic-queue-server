package com.ripyatakov.eqserver.controller;

import com.ripyatakov.eqserver.entity.Queue;
import com.ripyatakov.eqserver.entity.QueueListLive;
import com.ripyatakov.eqserver.entity.Review;
import com.ripyatakov.eqserver.entity.User;
import com.ripyatakov.eqserver.html.ReviewHtml;
import com.ripyatakov.eqserver.json.UserInQueue;
import com.ripyatakov.eqserver.managers.QueryManager;
import com.ripyatakov.eqserver.requests.AuthenticationRequest;
import com.ripyatakov.eqserver.requests.AuthorizationRequest;
import com.ripyatakov.eqserver.requests.ManagerRequest;
import com.ripyatakov.eqserver.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class ManagerController {
    @Autowired
    private UserService userService;
    @Autowired
    private QueueService queueService;
    @Autowired
    private OnlineQueueService onlineQueueService;
    @Autowired
    private QueueListLiveService queueListLiveService;
    @Autowired
    private QueryManager queryManager;
    @Autowired
    private ReviewService reviewService;

    private List<ReviewHtml> toReviewHtml(List<Review> reviews){
        return reviews.stream()
                .map(a -> new ReviewHtml(a.getRwQId(),a.getRwUId(),userService.getUserById(a.getRwUId()).getName(), a.getDescription(), a.getRate(), a.getTime()))
                .collect(Collectors.toList());
    }

    @RequestMapping(value = "/mlogin", method = RequestMethod.GET)
    public String login(Model model) {
        model.addAttribute("authorizationRequest", new AuthorizationRequest());
        return "authorization";
    }
    @PostMapping("/mlogin")
    public String startPage(@ModelAttribute AuthorizationRequest authorizationRequest, Model model) {
        List<User> userList = new ArrayList<>();
        User user = new User();
        try {
            user = userService.findByEmail(authorizationRequest.getEmail());
            authorizationRequest.setPassword(Hasher.sha256(authorizationRequest.getPassword()));
            if (user.getPassword().equals(authorizationRequest.getPassword()) && user.getRole().equals("manager")) {
                String token = TokenGenerator.getToken(authorizationRequest.getEmail(), authorizationRequest.getPassword());
                user.setToken(token);
                user = userService.updateUser(user);
            } else
                return "errorPage";
        }
        catch (Exception exc){
            exc.printStackTrace();
            return "errorPage";
        }
        userList.add(user);
        model.addAttribute("userList", userList);
        model.addAttribute("managerRequest", new ManagerRequest());
        //return "welcome";
        return "newWelcome";
    }
    @PostMapping("/mquery")
    public String query(@ModelAttribute ManagerRequest managerRequest, Model model) {
        try {
            model.addAttribute("authenticationRequest", new AuthenticationRequest(managerRequest.getToken()));
            List<User> userList = new ArrayList<>();
            User user = userService.getUserByToken(managerRequest.getToken());
            userList.add(user);
            model.addAttribute("userList", userList);
            return queryManager.executeQuery(managerRequest.getQuery(), model);
        } catch (Exception exc){
            exc.printStackTrace();
        }
        return "errorPage";
    }
    @PostMapping("/start")
    public String startPage(@ModelAttribute AuthenticationRequest authenticationRequest, Model model){
        try {
            User user = userService.getUserByToken(authenticationRequest.getToken());
            List<User> userList = new ArrayList<>();
            if (!user.getRole().equals("manager")) {
                return "errorPage";
            }
            userList.add(user);
            model.addAttribute("userList", userList);
            model.addAttribute("managerRequest", new ManagerRequest());
            return "newWelcome";
        } catch (Exception exc){
            exc.printStackTrace();
        }
        return "errorPage";
    }
    @PostMapping("/usersInQueue/{qid}")
    public String usersInQueue(@ModelAttribute AuthenticationRequest authenticationRequest, @PathVariable int qid, Model model){
        try {
            model.addAttribute("authenticationRequest", authenticationRequest);
            User user = userService.getUserByToken(authenticationRequest.getToken());
            List<User> userList = new ArrayList<>();
            if (!user.getRole().equals("manager")) {
                return "errorPage";
            }
            Queue queue = queueService.getQueueById(qid);
            List<QueueListLive> records = queueListLiveService.getQueueRecordings(queue);
            records.sort(QueueListLive::compareTo);
            List<UserInQueue> usersInQueue = new ArrayList<>();
            for (QueueListLive r : records) {
                User quser = userService.getUserById(r.getEqUId());
                usersInQueue.add(new UserInQueue(quser.getId(), r.getEqNumber(), quser.getName()));
            }
            model.addAttribute("queue", queue);
            model.addAttribute("queueRate", String.format("%.2f", reviewService.getAverageRating(queue)));
            model.addAttribute("usersInQueue", usersInQueue);
            return "newUsersInQueue";
        } catch (Exception exc){
            exc.printStackTrace();

        }
        return "errorPage";
    }
    @PostMapping("/makeAnAdmin/{uid}")
    public String makeAnAdmin(@ModelAttribute AuthenticationRequest authenticationRequest, @PathVariable int uid, Model model){
        try {
            model.addAttribute("authenticationRequest", authenticationRequest);
            User user = userService.getUserByToken(authenticationRequest.getToken());
            if (!user.getRole().equals("manager")) {
                return "errorPage";
            }
            User userToAdmin = userService.getUserById(uid);
            if (userToAdmin.getRole().equals("user")){
                userToAdmin.setRole("admin");
            }
            userService.saveUser(userToAdmin);
            return query(new ManagerRequest(authenticationRequest.getToken(), ("search users id " + userToAdmin.getId())), model);
        } catch (Exception exc){
            exc.printStackTrace();
        }
        return "errorPage";
    }

    @PostMapping("/queueReview/{qid}")
    public String queueReview(@ModelAttribute AuthenticationRequest authenticationRequest, @PathVariable int qid, Model model){
        try {
            model.addAttribute("authenticationRequest", authenticationRequest);
            User user = userService.getUserByToken(authenticationRequest.getToken());
            List<User> userList = new ArrayList<>();
            if (!user.getRole().equals("manager")) {
                return "errorPage";
            }
            Queue queue = queueService.getQueueById(qid);

            model.addAttribute("queue", queue);
            model.addAttribute("queueRate", String.format("%.2f", reviewService.getAverageRating(queue)));
            model.addAttribute("queueReviews", toReviewHtml(reviewService.findAllByQid(qid)));
            return "newQueueReview";
        } catch (Exception exc){
            exc.printStackTrace();

        }
        return "errorPage";
    }
    @PostMapping("/userReview/{uid}")
    public String userReview(@ModelAttribute AuthenticationRequest authenticationRequest, @PathVariable int uid, Model model){
        try {
            model.addAttribute("authenticationRequest", authenticationRequest);
            User user = userService.getUserByToken(authenticationRequest.getToken());
            List<User> userList = new ArrayList<>();
            if (!user.getRole().equals("manager")) {
                return "errorPage";
            }
            User user1 = userService.getUserById(uid);

            model.addAttribute("user", user1);
            model.addAttribute("userReviews", toReviewHtml(reviewService.findAllByUid(uid)));
            return "newUserReview";
        } catch (Exception exc){
            exc.printStackTrace();

        }
        return "errorPage";
    }
}
