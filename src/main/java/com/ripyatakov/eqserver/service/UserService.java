package com.ripyatakov.eqserver.service;

import com.ripyatakov.eqserver.entity.User;
import com.ripyatakov.eqserver.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    @Autowired
    private UserRepository repository;

    public User saveUser(User user){
        return repository.save(user);
    }

    public List<User> saveUsers(List<User> users){
        return repository.saveAll(users);
    }

    public User getUserById(int id){
        return repository.findById(id).orElse(null);
    }
    public List<User> getAllUsers(){
        return repository.findAll();
    }

    public List<User> getUserByName(String name){
        return repository.findByName(name);
    }

    public String deleteUser(int id){
        repository.deleteById(id);
        return "User deleted " + id;
    }

    public User getUserByToken(String token){
        return repository.findByToken(token);
    }

    public User updateUser(User user){
        User existingUser = repository.findById(user.getId()).orElse(null);
        existingUser.setName(user.getName());
        existingUser.setEmail(user.getEmail());
        existingUser.setPassword(user.getPassword());
        existingUser.setRole(user.getRole());
        existingUser.setToken(user.getToken());
        return repository.save(existingUser);
    }

    public User findByEmail(String email){
        return repository.findByEmail(email);
    }

    public List<User> findAllByName(String name){
        return repository.findByNameLike("%" + name + "%");
    }

    public List<User> findAllById(int id){
        return repository.findByIdLike(id);
    }

    public List<User> findAllByRole(String role){
        return repository.findByRoleLike("%" + role + "%");
    }

    public List<User> findAllByPage(int pageNumber, int pageSize){
        return repository.findAll(PageRequest.of(pageNumber, pageSize)).toList();
    }
}
