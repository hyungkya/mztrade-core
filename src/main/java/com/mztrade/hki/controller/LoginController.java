/*
package com.mztrade.hki.controller;

import com.mztrade.hki.dto.*;
import com.mztrade.hki.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class LoginController {
    private final UserService userService;

    public LoginController(UserService userService) {
        this.userService = userService;
    }

    */
/**
     * @param userDto
     * @return int uid : 회원정보 uid 반환
     *//*

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody UserDto userDto) {

        try{
            int uid = userService.saveUser(userDto);
            return
            new ResponseEntity<>(DefaultResponse.response(
                    StatusCode.OK,
                    ResponseMessage.CREATED_USER,
                    new RegisterResponse(uid)
            ), HttpStatus.OK);
            }catch (Exception e){
            return new ResponseEntity<>(
                    DefaultResponse.response(
                    StatusCode.BAD_REQUEST,
                    ResponseMessage.INTERNAL_SERVER_ERROR
            ), HttpStatus.BAD_REQUEST);
        }

    }

    */
/**
     * @param loginRequestDto
     * @return Token, 회원정보 반환
     *//*

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDto loginRequestDto) {

        log.info("로그인 유저요청 = " + loginRequestDto.getName());

        try{
            Integer uid = userService.getUser(loginRequestDto.getName()); // 유저 정보 확인

            log.info("로그인 완료");

            return new ResponseEntity<>(DefaultResponse.response(
                    StatusCode.OK,
                    ResponseMessage.LOGIN_SUCCESS,
                    new LoginResponseDto(loginRequestDto.getName(), uid)
            ), HttpStatus.OK);



        }catch (Exception e){

            log.info("로그인 실패");

            return new ResponseEntity<>(DefaultResponse.response(
                    StatusCode.BAD_REQUEST,
                    ResponseMessage.LOGIN_FAIL
            ), HttpStatus.BAD_REQUEST);
        }


    }
}
*/
