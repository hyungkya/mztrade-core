package com.mztrade.hki.controller;

import com.mztrade.hki.dto.LoginRequestDto;
import com.mztrade.hki.dto.LoginResponseDto;
import com.mztrade.hki.dto.UserDto;
import com.mztrade.hki.service.UserService;
import com.mztrade.hki.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Controller
public class LoginController {
    private final UserService userService;
    private final JwtUtil jwtUtil;

    public LoginController(UserService userService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    /**
     * @param UserDto
     * @return int uid : 회원정보 uid 반환
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody UserDto UserDto) {

        boolean isDuplicate = userService.checkUserDuplicate(UserDto.getName());

        if (isDuplicate) {
            return new ResponseEntity<>("중복된 계정입니다.", HttpStatus.BAD_REQUEST);
        } else {
            int uid = userService.saveUser(UserDto);
            return new ResponseEntity<>(uid, HttpStatus.OK);

        }


    }

    /**
     * @param loginRequestDto
     * @return boolean 권한정보 반환
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDto loginRequestDto) {

        try{
            userService.login(loginRequestDto.getName(), loginRequestDto.getPassword()); // 유저 정보 확인
            final String jwt = jwtUtil.generateToken(loginRequestDto.getName());
            return new ResponseEntity<>(new LoginResponseDto(jwt), HttpStatus.OK);
        }catch (Exception e){
            return new ResponseEntity<>("login failed: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }


    }
}
