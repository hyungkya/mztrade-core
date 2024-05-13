package com.mztrade.hki.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mztrade.hki.dto.BacktestParameter;
import com.mztrade.hki.dto.BacktestResultResponse;
import com.mztrade.hki.entity.Account;
import com.mztrade.hki.entity.User;
import com.mztrade.hki.entity.backtest.BacktestResult;
import com.mztrade.hki.repository.AccountRepository;
import com.mztrade.hki.repository.UserRepository;
import com.mztrade.hki.service.AccountService;
import com.mztrade.hki.service.BacktestService;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class BacktestController {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private BacktestService backtestService;
    private AccountService accountService;
    private ObjectMapper objectMapper;

    @Autowired
    public BacktestController(BacktestService backtestService, AccountService accountService,
            ObjectMapper objectMapper, AccountRepository accountRepository,
            UserRepository userRepository) {
        this.backtestService = backtestService;
        this.accountService = accountService;
        this.objectMapper = objectMapper;
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
    }

    @PostMapping("/backtest")
    public ResponseEntity<Boolean> createBacktest(@RequestBody BacktestParameter backtestParameter)
            throws JsonProcessingException {
        log.info(String.format("[POST] /backtest backtestParameter=%s", backtestParameter));

        int aid = backtestService.execute(backtestParameter);
        Account account = accountRepository.getReferenceById(aid);
        User user = userRepository.getReferenceById(backtestParameter.getUid());
        backtestService.create(BacktestResult.builder().account(account).user(user)
                .param(objectMapper.writeValueAsString(backtestParameter)).plratio(
                        backtestService.calculateFinalProfitLossRatio(
                                backtestParameter.getInitialBalance(), aid,
                                backtestParameter.parseEndDate())).build());

        return new ResponseEntity<>(true, HttpStatus.OK);
    }

    @GetMapping("/backtest/{aid}")
    public ResponseEntity<BacktestResultResponse> getBacktestResult(@PathVariable Integer aid) {
        BacktestResultResponse response = backtestService.get(aid);

        log.info(String.format("[GET] /backtest/aid=%s", aid));

        if (response == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/backtest/{aid}/param")
    public ResponseEntity<BacktestParameter> getBacktestParameter(@PathVariable Integer aid) {
        BacktestParameter backtestParameter = backtestService.getBacktestParameter(aid);

        log.info(String.format("[GET] /backtest/%s/param", aid));

        if (backtestParameter == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(backtestParameter, HttpStatus.OK);
    }

    @GetMapping("/backtest")
    public ResponseEntity<List<BacktestResultResponse>> getBacktestResults(
            @RequestParam Optional<Integer> uid,
            @RequestParam Optional<Integer> limit,
            @RequestParam(defaultValue = "") String title,
            @RequestParam Optional<List<Integer>> tids) {
        HttpStatus httpStatus = HttpStatus.OK;
        List<BacktestResultResponse> backtestResultResponses;
        if (uid.isPresent()) {
            if (tids.isPresent()) {
                backtestResultResponses = backtestService.searchBacktestResultByTags(uid.get(),
                        title, tids.get());
            } else {
                backtestResultResponses = backtestService.searchByTitle(uid.get(), title);
            }
        } else {
            backtestResultResponses = backtestService.getAllByPlratioDesc();
        }
        if (limit.isPresent()) {
            backtestResultResponses = backtestResultResponses.stream()
                    .sorted(Comparator.comparing(BacktestResultResponse::getPlratio).reversed())
                    .limit(limit.get()).toList();
        }
        return new ResponseEntity<>(backtestResultResponses, httpStatus);
    }

    @PutMapping("/backtest/{aid}")
    public ResponseEntity<Boolean> updateBacktestResult(@PathVariable Integer aid,
                                                        @RequestBody BacktestParameter backtestParameter) throws JsonProcessingException {
        log.info(String.format("[PUT] /backtest/%s", aid));
        backtestService.update(aid, objectMapper.writeValueAsString(backtestParameter));
        return new ResponseEntity<>(true, HttpStatus.OK);
    }

    @DeleteMapping("/backtest/{aid}")
    public ResponseEntity<Boolean> deleteBacktestResult(@PathVariable Integer aid) {
        log.info(String.format("[DELETE] /backtest/%s", aid));
        accountService.deleteAccount(aid);
        return new ResponseEntity<>(true, HttpStatus.OK);
    }
}
