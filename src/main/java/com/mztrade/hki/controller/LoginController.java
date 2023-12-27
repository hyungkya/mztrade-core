package com.mztrade.hki.controller;

import com.mztrade.hki.entity.LoginRequest;
import com.mztrade.hki.entity.RegisterRequest;
import com.mztrade.hki.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Controller
public class LoginController {
    private UserService userService;

    @Autowired
    public LoginController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<Integer> register(
            @RequestBody RegisterRequest registerRequest
    ) {
        System.out.println("POST /register method has been called.");

        int uid = userService.createUser(registerRequest.getName(), registerRequest.getPassword());
        return new ResponseEntity<>(uid, HttpStatus.OK);
    }

    @PostMapping("/login")
    public ResponseEntity<Boolean> login(
            @RequestBody LoginRequest loginRequest
    ) {
        System.out.println("POST /login method has been called.");
        boolean authenticated = userService.login(loginRequest.getName(), loginRequest.getPassword());
        return new ResponseEntity<>(authenticated, HttpStatus.OK);
    }
}
