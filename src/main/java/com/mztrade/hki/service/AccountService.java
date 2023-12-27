package com.mztrade.hki.service;

import com.mztrade.hki.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AccountService {
    private final AccountRepository accountRepository;

    @Autowired
    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public int createAccount(int uid) {
        return accountRepository.createAccount(uid);
    }

    public List<Integer> getAll(int uid) {
        return accountRepository.getAll(uid);
    }
    public long getBalance(int aid) {
        return accountRepository.getBalance(aid);
    }

    public boolean deposit(Integer aid, Long amount) {
        Long currentBalance = accountRepository.getBalance(aid);
        accountRepository.updateBalance(aid, currentBalance + amount);
        return true;
    }

    public boolean withdraw(Integer aid, Long amount) {
        Long currentBalance = accountRepository.getBalance(aid);
        if (currentBalance >= amount) {
            accountRepository.updateBalance(aid, currentBalance - amount);
            return true;
        }
        return false;
    }
}
