package com.ripyatakov.eqserver.controller;

import com.ripyatakov.eqserver.entity.User;
import com.ripyatakov.eqserver.requests.AuthorizationRequest;
import com.ripyatakov.eqserver.service.Hasher;
import com.ripyatakov.eqserver.service.TokenGenerator;
import com.ripyatakov.eqserver.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthorizationController {
    @Autowired
    private UserService userService;

    @PostMapping("/register/{role}")
    public ResponseEntity register(@RequestBody AuthorizationRequest registrationRequest, @PathVariable String role){
        return register(role, registrationRequest);
    }

    @PostMapping("/register")
    public ResponseEntity register(@RequestBody AuthorizationRequest registrationRequest){
        return register("user", registrationRequest);
    }

    private ResponseEntity register(String role, AuthorizationRequest registrationRequest){
        try {
            registrationRequest.setPassword(Hasher.sha256(registrationRequest.getPassword()));
            String token = TokenGenerator.getToken(registrationRequest.getEmail(), registrationRequest.getPassword());
            User newUser = new User(0, registrationRequest.getName(),
                    registrationRequest.getEmail(),
                    registrationRequest.getPassword(),
                    token,
                    role
            );
            return new ResponseEntity<>(userService.saveUser(newUser), HttpStatus.CREATED);
        }
        catch (Exception exc){
            exc.printStackTrace();
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Existing email");
        }
    }

    @PostMapping("/login")
    public ResponseEntity login(@RequestBody AuthorizationRequest loginRequest){
        try {
            User user = userService.findByEmail(loginRequest.getEmail());
            loginRequest.setPassword(Hasher.sha256(loginRequest.getPassword()));
            if (user.getPassword().equals(loginRequest.getPassword())) {
                String token = TokenGenerator.getToken(loginRequest.getEmail(), loginRequest.getPassword());
                user.setToken(token);
                user = userService.updateUser(user);
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Wrong password");
            }
            return ResponseEntity.status(HttpStatus.OK).body(user);
        }
        catch (Exception exc){
            exc.printStackTrace();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No such email found");
        }

    }
}
