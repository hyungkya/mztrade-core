package com.mztrade.hki.controller;

import com.mztrade.hki.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class AccountController {

    private AccountService accountService;

    @Autowired
    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping("/account")
    public ResponseEntity<List<Integer>> getAccounts(
            @RequestParam int uid
    ) {
        return new ResponseEntity<>(accountService.getAll(uid), HttpStatus.OK);
    }
}
