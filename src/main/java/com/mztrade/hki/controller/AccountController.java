package com.mztrade.hki.controller;

import com.mztrade.hki.dto.OrderResponse;
import com.mztrade.hki.dto.PositionResponse;
import com.mztrade.hki.service.AccountService;
import com.mztrade.hki.service.OrderService;
import com.mztrade.hki.service.StatisticService;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class AccountController {

    private final OrderService orderService;
    private final StatisticService statisticService;
    private AccountService accountService;

    @Autowired
    public AccountController(AccountService accountService, OrderService orderService,
            StatisticService statisticService) {
        this.accountService = accountService;
        this.orderService = orderService;
        this.statisticService = statisticService;
    }

    @GetMapping("/account")
    public ResponseEntity<List<Integer>> getAccounts(
            @RequestParam int uid
    ) {
        return new ResponseEntity<>(accountService.getAllBacktestAccountIds(uid), HttpStatus.OK);
    }

    @GetMapping("/account/{aid}/position")
    public ResponseEntity<List<PositionResponse>> getPositions(@PathVariable Integer aid) {
        return new ResponseEntity<>(orderService.getPositions(aid), HttpStatus.OK);
    }

    @GetMapping("/account/{aid}/balance")
    public ResponseEntity<Long> getBalance(@PathVariable Integer aid) {
        return new ResponseEntity<>(accountService.getBalance(aid), HttpStatus.OK);
    }

    @GetMapping("/account/{aid}/order")
    public ResponseEntity<List<OrderResponse>> getOrderHistory(@PathVariable Integer aid) {
        return new ResponseEntity<>(orderService.getOrderHistory(aid), HttpStatus.OK);
    }
}
