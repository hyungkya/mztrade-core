package com.mztrade.hki.controller;


import com.mztrade.hki.dto.EmailCheckDto;
import com.mztrade.hki.dto.EmailRequestDto;
import com.mztrade.hki.service.EmailService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;


@Slf4j
@RestController
@RequiredArgsConstructor
public class EmailController {
    private final EmailService emailService;

    @PostMapping("/send-email")
    public ResponseEntity<Boolean> sendEmail(@RequestBody @Valid EmailRequestDto emailDto, BindingResult bindingResult) {
        log.info(String.format("[POST] /send-email (email=%s) has been called.", emailDto.getEmail()));
        if (bindingResult.hasErrors()) {
            return new ResponseEntity<>(false, HttpStatus.BAD_REQUEST);
        } else {
            if (emailService.joinEmail(emailDto.getEmail())) {
                return new ResponseEntity<>(true, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(false, HttpStatus.OK);
            }
        }
    }


@PostMapping("/check-email")
public ResponseEntity<String> checkEmail(@RequestBody @Valid EmailCheckDto emailCheckDto, BindingResult bindingResult) {
    log.info(String.format("[POST] /check-email (email=%s, code=%s) has been called.", emailCheckDto.getEmail(), emailCheckDto.getAuthNum()));
    Boolean Checked = emailService.CheckAuthNum(emailCheckDto.getEmail(), emailCheckDto.getAuthNum());
    if (Checked) {
        return new ResponseEntity<>("SUCCESS", HttpStatus.OK);
    } else {
        return new ResponseEntity<>("FAILED", HttpStatus.OK);
    }
}
}