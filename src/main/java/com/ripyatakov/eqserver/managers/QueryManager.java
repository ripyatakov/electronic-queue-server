package com.ripyatakov.eqserver.managers;

import com.ripyatakov.eqserver.entity.Queue;
import com.ripyatakov.eqserver.entity.User;
import com.ripyatakov.eqserver.service.OnlineQueueService;
import com.ripyatakov.eqserver.service.QueueListLiveService;
import com.ripyatakov.eqserver.service.QueueService;
import com.ripyatakov.eqserver.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import javax.annotation.PostConstruct;
import java.util.*;

@Service
@RequiredArgsConstructor
@EnableScheduling
public class QueryManager {
    @Autowired
    private UserService userService;
    @Autowired
    private QueueService queueService;
    @Autowired
    private QueueListLiveService queueListLiveService;
    @Autowired
    private final OnlineQueueService onlineQueuesService;

    private List<String> commands;

    private List<String> objects;

    private List<String> userParams;

    private List<String> queueParams;

    @PostConstruct
    private void init() {
        commands = new ArrayList<>();
        commands.add("get");
        commands.add("search");
        commands.add("stat");

        objects = new ArrayList<>();
        objects.add("users");
        objects.add("queues");

        userParams = new ArrayList<>();
        userParams.add("id");
        userParams.add("name");
        userParams.add("role");

        queueParams = new ArrayList<>();
        queueParams.add("id");
        queueParams.add("title");
        queueParams.add("ownerId");
        queueParams.add("type");
        queueParams.add("status");

    }


    public String executeQuery(String query, Model model) {
        try {
            List<String> queryParts = formatString(query);
            if (commands.get(0).equals(queryParts.get(0))) {
                return executeGet(queryParts, model);
            } else
                if (commands.get(1).equals(queryParts.get(0))){
                    return executeSearch(queryParts, model);
                } else
                    if (commands.get(2).equals(queryParts.get(0))){
                        return executeStat(queryParts, model);
                    }
        } catch (Exception exc) {
            return "errorPage";
        }
        return "errorPage";
    }

    private String executeGet(List<String> params, Model model) {
        if (!objects.contains(params.get(1)))
            throw new IllegalArgumentException();
        String obj = params.get(1);
        int pageNumber = 0, pageSize = 100;
        if (obj.equals(objects.get(0))) {
            if (params.size() >= 4) {
                pageNumber = Integer.parseInt(params.get(2));
                pageSize = Integer.parseInt(params.get(3));
            } else if (params.size() >= 3) {
                pageSize = Integer.parseInt(params.get(2));
            }
            List<User> userList = userService.findAllByPage(pageNumber, pageSize);
            model.addAttribute("userList", userList);
            return "userList";
        } else if (obj.equals(objects.get(1))){
            if (params.size() >= 4) {
                pageNumber = Integer.parseInt(params.get(2));
                pageSize = Integer.parseInt(params.get(3));
            } else if (params.size() >= 3) {
                pageSize = Integer.parseInt(params.get(2));
            }
            List<Queue> queueList = queueService.findAllByPage(pageNumber, pageSize);
            model.addAttribute("queueList", queueList);
            return "queueList";
        }
        return "errorPage";
    }

    private String executeSearch(List<String> params, Model model){
        if (!objects.contains(params.get(1)))
            throw new IllegalArgumentException();
        String obj = params.get(1);
        String param = params.get(2);
        String value = params.get(3);
        if (obj.equals(objects.get(0))){
            List<User> userList = new ArrayList<>();
            //users
            if (param.equals(userParams.get(0))){
                //id
                userList.addAll(userService.findAllById(Integer.parseInt(value)));
            } else if (param.equals(userParams.get(1))){
                //name
                userList.addAll(userService.findAllByName(value));
            } else if (param.equals(userParams.get(2))){
                //role
                userList.addAll(userService.findAllByRole(value));
            }
            model.addAttribute("userList", userList);
            return "userList";
        } else if (obj.equals(objects.get(1))){
            //queues
            List<Queue> queueList = new ArrayList<>();
            if (param.equals(queueParams.get(0))){
                //id
                queueList.addAll(queueService.findAllById(Integer.parseInt(value)));
            } else if (param.equals(queueParams.get(1))){
                //title
                queueList.addAll(queueService.findAllByEqTitle(value));
            } else if (param.equals(queueParams.get(2))){
                //ownerId
                queueList.addAll(queueService.getQueuesByOwnerId(Integer.parseInt(value)));
            } else if (param.equals(queueParams.get(3))){
                //type
                queueList.addAll(queueService.findAllByEqTypeLike(value));
            } else if (param.equals(queueParams.get(4))){
                //status
                queueList.addAll(queueService.findAllByEqStatusLike(value));
            }
            model.addAttribute("queueList", queueList);
            return "queueList";
        }
        return null;
    }

    private String executeStat(List<String> params, Model model){
        return null;
    }

    private List<String> formatString(String query) {
        List<String> answ = new ArrayList<>();
        StringBuilder sb = new StringBuilder(query);
        for (int i = 0; i < sb.length(); i++) {
            if (isSign(sb.charAt(i))) {
                sb.insert(i, ' ');
                i++;
                sb.insert(i + 1, ' ');
                i++;
            }
        }
        query = sb.toString();
        query = query.trim().replaceAll(" +", " ");
        answ.addAll(Arrays.asList(query.split(" ")));
        return answ;
    }

    private boolean isSign(char c) {
        return false;
    }
}
