package com.ripyatakov.eqserver.controllers;

import com.ripyatakov.eqserver.entities.User;
import com.ripyatakov.eqserver.json.ResponseMessage;
import com.ripyatakov.eqserver.requests.AuthenticationRequest;
import com.ripyatakov.eqserver.requests.AuthorizationRequest;
import com.ripyatakov.eqserver.requests.UpdateUserRequest;
import com.ripyatakov.eqserver.services.Hasher;
import com.ripyatakov.eqserver.services.TokenGenerator;
import com.ripyatakov.eqserver.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class AuthorizationController {
    @Autowired
    private UserService userService;
    private User getUser(AuthenticationRequest authenticationRequest) {
        return userService.getUserByToken(authenticationRequest.getToken());
    }
    @PostMapping("/updateUser")
    public ResponseEntity updateUser(@RequestBody UpdateUserRequest updateUserRequest){
        ResponseMessage responseMessage;
        try {
            User user = getUser(updateUserRequest);
            responseMessage = new ResponseMessage("Somebody was authorized by your password");
            if (user == null)
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseMessage);
            if (updateUserRequest.getPassword() != null){
                user.setPassword(Hasher.sha256(updateUserRequest.getPassword()));
            }
            if (updateUserRequest.getName() != null){
                user.setName(updateUserRequest.getName());
            }
            if (updateUserRequest.getEmail() != null){
                user.setEmail(updateUserRequest.getEmail());
            }
            return ResponseEntity.status(HttpStatus.OK).body(userService.saveUser(user));

        } catch (Exception exc){
            responseMessage = new ResponseMessage("Somebody was authorized by your password");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseMessage);
        }
    }
    @PostMapping("/register/{role}")
    public ResponseEntity register(@RequestBody AuthorizationRequest registrationRequest, @PathVariable String role){
        return register(role, registrationRequest);
    }

    @PostMapping("/register")
    public ResponseEntity register(@RequestBody AuthorizationRequest registrationRequest){
        return register("user", registrationRequest);
    }

    private ResponseEntity register(String role, AuthorizationRequest registrationRequest){
        ResponseMessage responseMessage;
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
            responseMessage = new ResponseMessage("Existing email");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(responseMessage);
        }
    }

    @PostMapping("/login")
    public ResponseEntity login(@RequestBody AuthorizationRequest loginRequest){
        ResponseMessage responseMessage;
        try {
            User user = userService.findByEmail(loginRequest.getEmail());
            loginRequest.setPassword(Hasher.sha256(loginRequest.getPassword()));
            if (user.getPassword().equals(loginRequest.getPassword())) {
                String token = TokenGenerator.getToken(loginRequest.getEmail(), loginRequest.getPassword());
                user.setToken(token);
                user = userService.updateUser(user);
            } else {
                responseMessage = new ResponseMessage("Wrong password");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(responseMessage);
            }
            return ResponseEntity.status(HttpStatus.OK).body(user);
        }
        catch (Exception exc){
            exc.printStackTrace();
            responseMessage = new ResponseMessage("No such email found");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseMessage);
        }
    }
}
