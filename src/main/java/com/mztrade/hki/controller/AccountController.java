package com.mztrade.hki.controller;

import com.mztrade.hki.Util;
import com.mztrade.hki.entity.AccountHistory;
import com.mztrade.hki.service.AccountService;
import com.mztrade.hki.service.StatisticService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@Slf4j
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
        log.info(String.format("[GET] /account?uid=%d has been called.", uid));
        return new ResponseEntity<>(accountService.getAll(uid), HttpStatus.OK);
    }

    @GetMapping("/compare-chart/plratio")
    public ResponseEntity<Map<Integer,Map<LocalDateTime, Long>>> getPlRatio(
            @RequestParam List<Integer> aids
    ) {
        Map<Integer,Map<LocalDateTime,Long>> mapList = new HashMap<>();

        for(Integer aid : aids) {
            mapList.put(aid,accountService.getPlRatio(aid));
        }

        log.info(String.format("[GET] /compare-chart/plratio/aids=%s",aids));

        return new ResponseEntity<>(mapList, HttpStatus.OK);
    }

}
