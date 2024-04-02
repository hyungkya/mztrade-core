package com.mztrade.hki.controller;

import com.mztrade.hki.dto.UserDto;
import com.mztrade.hki.service.AccountService;
import com.mztrade.hki.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@Slf4j
public class UserController {

    private UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/user")
    public ResponseEntity<Integer> saveUser(
            @RequestParam String firebaseUid,
            @RequestParam String name
    ) {
        log.info(String.format("[POST] /saveUser?firebaseUid=%s has been called.", firebaseUid));
        return new ResponseEntity<>(
                userService.saveUser(
                        UserDto.builder()
                                .firebaseUid(firebaseUid)
                                .name(name)
                                .build()
                ), HttpStatus.OK);
    }

    @GetMapping("/user")
    public ResponseEntity<UserDto> findUser(
            @RequestParam String firebaseUid
    ) {
        log.info(String.format("[GET] /findUser?firebaseUid=%s has been called.", firebaseUid));
        try {
            return new ResponseEntity<>(userService.getUser(firebaseUid), HttpStatus.OK);
        } catch (UsernameNotFoundException e) {
            return new ResponseEntity<>(null, HttpStatus.OK);
        }
    }

    @DeleteMapping("/user")
    public ResponseEntity<?> deleteUser(
            @RequestParam String firebaseUid
    ) {
        log.info(String.format("[DELETE-XXX] /deleteUser?firebaseUid=%s has been called.", firebaseUid));
        return new ResponseEntity<>(null, HttpStatus.OK);
    }

}
