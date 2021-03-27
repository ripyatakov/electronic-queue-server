package com.ripyatakov.eqserver.service;

import com.ripyatakov.eqserver.entity.User;
import com.ripyatakov.eqserver.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    public User saveUsers(User user){
        return userRepository.save(user);
    }

    public List<User> saveUsers(List<User> users){
        return userRepository.saveAll(users);
    }

    public User getUserById(int id){
        return userRepository.findById(id).orElse(null);
    }
    public List<User> getAllUsers(){
        return userRepository.findAll();
    }

    public List<User> getUserByName(String name){
        return userRepository.findByName(name);
    }

    public String deleteUser(int id){
        userRepository.deleteById(id);
        return "User deleted " + id;
    }

    public User updateUser(User user){
        User existingUser = userRepository.findById(user.getId()).orElse(null);
        existingUser.setName(user.getName());
        existingUser.setEmail(user.getEmail());
        return userRepository.save(existingUser);
    }

}
