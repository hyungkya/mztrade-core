package com.mztrade.hki.service;

import com.mztrade.hki.entity.AccountHistory;
import com.mztrade.hki.repository.AccountRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class AccountService {
    private final AccountRepository accountRepository;

    @Autowired
    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public int createAccount(int uid) {
        int aid = accountRepository.createAccount(uid);
        log.debug(String.format("[AccountService] createAccount(uid: %d) -> aid: %d", uid, aid));
        return aid;
    }
    public boolean deleteAccount(int aid) {
        boolean isDeleted = accountRepository.deleteAccount(aid);
        log.debug(String.format("[AccountService] deleteAccount(aid: %d) -> isDeleted: %b", aid, isDeleted));
        return isDeleted;
    }

    public List<Integer> getAll(int uid) {
        List<Integer> accounts = accountRepository.getAll(uid);
        log.debug(String.format("[AccountService] getAll(uid: %d) -> accounts: %s", uid, accounts.toString()));
        return accounts;
    }
    public long getBalance(int aid) {
        long balance = accountRepository.getBalance(aid);
        log.debug(String.format("[AccountService] getBalance(aid: %d) -> balance: %d", aid, balance));
        return balance;
    }

    public boolean deposit(Integer aid, Long amount) {
        Long currentBalance = accountRepository.getBalance(aid);
        accountRepository.updateBalance(aid, currentBalance + amount);
        log.debug(String.format("[AccountService] deposit(aid: %d, amount: %d) -> success: %b", aid, amount, true));
        return true;
    }

    public boolean withdraw(Integer aid, Long amount) {
        boolean isSuccess = false;
        Long currentBalance = accountRepository.getBalance(aid);
        if (currentBalance >= amount) {
            accountRepository.updateBalance(aid, currentBalance - amount);
            isSuccess = true;
        }
        log.debug(String.format("[AccountService] withdraw(aid: %d, amount: %d) -> success: %b", aid, amount, isSuccess));
        return isSuccess;
    }

    public boolean createAccountHistory(int aid, LocalDateTime date, long balance) {
        boolean isSuccess = accountRepository.createAccountHistory(aid,date,balance);
        log.debug(String.format("[AccountService] createAccountHistory(aid: %d, date: %s, balance:%d) -> isSuccess: %b", aid, date, balance, isSuccess));
        return isSuccess;
    }

    public Map<LocalDateTime,Long> getPlRatio(Integer aid) {

        List<AccountHistory> accountHistories = accountRepository.getAccountHistory(aid);
        Map<LocalDateTime,Long> resultMap = new HashMap<>();

        if(!accountHistories.isEmpty()) {
            for(AccountHistory accountHistory : accountHistories) {
                resultMap.put(accountHistory.getDate(),accountHistory.getBalance());
            }
        }

        log.debug(String.format("[AccountService] getPlRatio(aid: %d) -> isSuccess: %b", aid, accountHistories));

        return resultMap;
    }

}
