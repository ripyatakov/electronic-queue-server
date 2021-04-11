package com.ripyatakov.eqserver.managers;

import com.ripyatakov.eqserver.entity.Queue;
import com.ripyatakov.eqserver.entity.QueueListLive;
import com.ripyatakov.eqserver.entity.User;
import com.ripyatakov.eqserver.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;

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
    @Autowired
    private final ReviewService reviewService;

    private List<String> commands;

    private List<String> objects;

    private List<String> userParams;

    private List<String> queueParams;

    private List<String> statParams;

    private List<String> statObjects;



    @PostConstruct
    private void init() {
        commands = new ArrayList<>();
        commands.add("get");
        commands.add("search");
        commands.add("stat");
        commands.add("promote");

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

        statObjects = new ArrayList<>();
        statObjects.add("waitingTime");
        statObjects.add("count");
        statObjects.add("rate");
        statObjects.add("usersInQueues");
        statObjects.add("propUserTypes");

        statParams = new ArrayList<>();
        statParams.add("ownerId");
        statParams.add("queueId");

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
                    } else
                    if (commands.get(3).equals(queryParts.get(0))){
                        return executePromote(queryParts, model);
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
            return "newUserList";
        } else if (obj.equals(objects.get(1))){
            if (params.size() >= 4) {
                pageNumber = Integer.parseInt(params.get(2));
                pageSize = Integer.parseInt(params.get(3));
            } else if (params.size() >= 3) {
                pageSize = Integer.parseInt(params.get(2));
            }
            List<Queue> queueList = queueService.findAllByPage(pageNumber, pageSize);
            model.addAttribute("queueList", queueList);
            return "newQueueList";
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
        params.add("ownerId");
        params.add("23");
        String obj = params.get(1);
        String param = params.get(2);
        String value = params.get(3);
        if (!statObjects.contains(obj) || !statParams.contains(param))
            throw new IllegalArgumentException();
        if (obj.equals(statObjects.get(0))){
            //averageWaitingTime
            if (param.equals(statParams.get(0))){
                //ownerId
                int val = Integer.parseInt(value);
                List<Queue> queues = queueService.getQueuesByOwnerId(val);
                queues.sort(new Comparator<Queue>() {
                    @Override
                    public int compare(Queue o1, Queue o2) {
                        return new Integer(o1.getId()).compareTo(o2.getId());
                    }
                });
                List<Integer> X = queues.stream().map(a -> a.getId()).collect(Collectors.toList());
                List<Double> Y = queues.stream().map(a -> a.getEqAverageWaitingTime()).collect(Collectors.toList());
                String title = "Statistic serve time by owner id";
                String xName = "queue id";
                String yName = "average serve time";
                model.addAttribute("title", title);
                model.addAttribute("xName", xName);
                model.addAttribute("Type", "scatter");
                model.addAttribute("yName", yName);
                model.addAttribute("X", X);
                model.addAttribute("Y", Y);
            }
            if (param.equals(statParams.get(1))){
                //queueId
                int val = Integer.parseInt(value);
                List<QueueListLive> records = queueListLiveService.getQueueRecordings(val);
                records.sort(QueueListLive::compareTo);
                List<Date> X = records.stream().map(a -> a.getEqStartServeTime()).collect(Collectors.toList());
                List<Float> Y = records.stream()
                        .map(
                        a -> (float)((a.getEqStartServeTime().getTime() - a.getEqEnterTime().getTime())/1000.0/60.0) - a.getEqServeTimeMin()
                )
                        .map(a -> (a< 0)?0:a )
                        .collect(Collectors.toList());
                String title = "Waiting time";
                String xName = "time start user serving";
                String yName = "waiting time";
                model.addAttribute("title", title);
                model.addAttribute("xName", xName);
                model.addAttribute("Type", "scatter");
                model.addAttribute("yName", yName);
                model.addAttribute("X", X);
                model.addAttribute("Y", Y);
            }
        }
        if (obj.equals(statObjects.get(1))){
            //count
            if (param.equals(statParams.get(0))){
                //ownerId
                int val = Integer.parseInt(value);
                List<Queue> queues = queueService.getQueuesByOwnerId(val);
                queues.sort(new Comparator<Queue>() {
                    @Override
                    public int compare(Queue o1, Queue o2) {
                        return new Integer(o1.getId()).compareTo(o2.getId());
                    }
                });
                //queueListLiveService.queueSize();
                List<Integer> X = queues.stream().map(a -> a.getId()).collect(Collectors.toList());
                List<Integer> Y = queues.stream().map(a -> (int)queueListLiveService.queueSize(a)).collect(Collectors.toList());
                String title = "Statistic by owner id";
                String xName = "queue id";
                String yName = "amount registered users";
                model.addAttribute("title", title);
                model.addAttribute("xName", xName);
                model.addAttribute("yName", yName);
                model.addAttribute("Type", "scatter");
                model.addAttribute("X", X);
                model.addAttribute("Y", Y);
            }
            if (param.equals(statParams.get(1))){
                //queueId
                int val = Integer.parseInt(value);
                List<String> X = new ArrayList<>();
                for (int i = 0; i < 24; i++) {
                    if (new Integer(i).toString().length() < 2){
                        X.add("0" + i+ ":00");
                    } else
                        X.add(i + ":00");
                }
                List<Integer> Y = queueListLiveService.usersRegistersByHours(val);
                String title = "Registered users statistic";
                String xName = "time";
                String yName = "count registered users";
                model.addAttribute("title", title);
                model.addAttribute("xName", xName);
                model.addAttribute("Type", "bar");
                model.addAttribute("yName", yName);
                model.addAttribute("X", X);
                model.addAttribute("Y", Y);
            }
        }
        if (obj.equals(statObjects.get(2))) {
            //rate
            if (param.equals(statParams.get(0))){
                //ownerId
                int val = Integer.parseInt(value);
                List<Queue> queues = queueService.getQueuesByOwnerId(val);
                queues.sort(new Comparator<Queue>() {
                    @Override
                    public int compare(Queue o1, Queue o2) {
                        return new Integer(o1.getId()).compareTo(o2.getId());
                    }
                });

                List<Integer> X = queues.stream().map(a -> a.getId()).collect(Collectors.toList());
                List<Double> Y = queues.stream().map(a ->  reviewService.getAverageRating(a)).collect(Collectors.toList());
                String title = "Statistic average rating by queues";
                String xName = "queue id";
                String yName = "average rating";
                model.addAttribute("title", title);
                model.addAttribute("xName", xName);
                model.addAttribute("Type", "bar");
                model.addAttribute("yName", yName);
                model.addAttribute("X", X);
                model.addAttribute("Y", Y);
            }
        }
        if (obj.equals(statObjects.get(3))){
            //usersInQueues
            List<Queue> allQueues = queueService.getAllQueues();
            List<Integer> Y = allQueues.stream().map(a -> (int)queueListLiveService.queueSize(a)).collect(Collectors.toList());
            List<Integer> X = allQueues.stream().map(a -> a.getId()).collect(Collectors.toList());
            String title = "Registered users statistic";
            String xName = "queue id";
            String yName = "amount registered users";
            model.addAttribute("title", title);
            model.addAttribute("xName", xName);
            model.addAttribute("Type", "bar");
            model.addAttribute("yName", yName);
            model.addAttribute("X", X);
            model.addAttribute("Y", Y);
        }
        if (obj.equals(statObjects.get(4))) {
            //propUserTypes
            if (param.equals(statParams.get(0))) {
                //ownerId
                int val = Integer.parseInt(value);
                HashMap<String, Integer> hm = new HashMap<>();
                List<Queue> queues = queueService.getQueuesByOwnerId(val);
                for (Queue q: queues){
                    val = q.getId();
                    List<QueueListLive> queueListLives = queueListLiveService.getQueueRecordings(val);
                    List<User> users = queueListLives.stream().map(a -> userService.getUserById(a.getEqUId())).collect(Collectors.toList());
                    for (int i = 0; i < users.size(); i++) {
                        if (hm.containsKey(users.get(i).getRole())){
                            hm.put(users.get(i).getRole(), hm.get(users.get(i).getRole()) + 1);
                        } else
                            hm.put(users.get(i).getRole(), 1);
                    }
                }
                Set<String> X = hm.keySet();
                Collection<Integer> Y = hm.values();
                String title = "Statistic by " + queues.size() + " queues";
                String xName = "queue id";
                String yName = "average rating";
                model.addAttribute("title", title);
                model.addAttribute("X", X);
                model.addAttribute("Y", Y);
                return "pie";
            } else
            if (param.equals(statParams.get(1))) {
                //queueId
                int val = Integer.parseInt(value);
                List<QueueListLive> queueListLives = queueListLiveService.getQueueRecordings(val);
                List<User> users = queueListLives.stream().map(a -> userService.getUserById(a.getEqUId())).collect(Collectors.toList());
                HashMap<String, Integer> hm = new HashMap<>();
                for (int i = 0; i < users.size(); i++) {
                    if (hm.containsKey(users.get(i).getRole())){
                        hm.put(users.get(i).getRole(), hm.get(users.get(i).getRole()) + 1);
                    } else
                        hm.put(users.get(i).getRole(), 1);
                }

                Set<String> X = hm.keySet();
                Collection<Integer> Y = hm.values();
                String title = "Statistic " + val + " queue";
                String xName = "queue id";
                String yName = "average rating";
                model.addAttribute("title", title);
                model.addAttribute("X", X);
                model.addAttribute("Y", Y);
                return "pie";
            }
        }
        return "graph";
    }

    private String executePromote(List<String> params, Model model){
        String obj = params.get(1);
        String param = params.get(2);
        String value = params.get(3);
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
