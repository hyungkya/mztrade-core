package com.mztrade.hki.service;

import com.mztrade.hki.dto.AccountResponse;
import com.mztrade.hki.entity.Account;
import com.mztrade.hki.entity.AccountHistory;
import com.mztrade.hki.entity.User;
import com.mztrade.hki.repository.AccountHistoryRepository;
import com.mztrade.hki.repository.AccountRepository;
import com.mztrade.hki.repository.UserRepository;
import jakarta.transaction.Transactional;
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
    private final AccountHistoryRepository accountHistoryRepository;
    private final UserRepository userRepository;

    @Autowired
    public AccountService(AccountRepository accountRepository, AccountHistoryRepository accountHistoryRepository, UserRepository userRepository) {
        this.accountRepository = accountRepository;
        this.accountHistoryRepository = accountHistoryRepository;
        this.userRepository = userRepository;
    }

    public int createAccount(int uid) {
        User user = userRepository.getReferenceById(uid);
        Account account = accountRepository.save(Account.builder().user(user).build());
        log.debug(String.format("[AccountService] createAccount(uid: %d) -> aid: %d", uid, account.getAid()));
        return account.getAid();
    }
    public void deleteAccount(int aid) {
        accountRepository.deleteById(aid);
        log.debug(String.format("[AccountService] deleteAccount(aid: %d)", aid));
    }

    public List<Integer> getAllBacktestAccountIds(int uid) {
        List<Integer> accountIds = accountRepository.findByUserUidAndType(uid, "BACKTEST").stream()
                .map((a) -> a.getAid()).toList();
        log.debug(String.format("[AccountService] getAll(uid: %d) -> accounts: %s", uid, accountIds));
        return accountIds;
    }
    public long getBalance(int aid) {
        Long balance = accountRepository.findById(aid).get().getBalance();
        log.debug(String.format("[AccountService] getBalance(aid: %d) -> balance: %d", aid, balance));
        return balance;
    }

    public boolean deposit(Integer aid, Long amount) {
        Account account = accountRepository.findById(aid).orElseThrow();
        account.setBalance(account.getBalance() + amount);
        accountRepository.save(account);
        log.debug(String.format("[AccountService] deposit(aid: %d, amount: %d) -> success: %b", aid, amount, true));
        return true;
    }

    public boolean withdraw(Integer aid, Long amount) {
        boolean isSuccess = false;
        Account account = accountRepository.findById(aid).orElseThrow();
        if (account.getBalance() >= amount) {
            account.setBalance(account.getBalance() - amount);
            accountRepository.save(account);
            isSuccess = true;
        }
        log.debug(String.format("[AccountService] withdraw(aid: %d, amount: %d) -> success: %b", aid, amount, isSuccess));
        return isSuccess;
    }

    public void createAccountHistory(int aid, LocalDateTime date, long balance) {
        accountHistoryRepository.save(
                AccountHistory.builder().aid(aid).date(date).balance(balance).build()
        );
        log.debug(String.format("[AccountService] createAccountHistory(aid: %d, date: %s, balance:%d)", aid, date, balance));
    }

    @Transactional
    public void createAccountHistories(int aid, Map<LocalDateTime, Long> balances) {
        for (Map.Entry<LocalDateTime, Long> entry : balances.entrySet()) {
            accountHistoryRepository.save(
                    AccountHistory.builder().aid(aid).date(entry.getKey()).balance(entry.getValue()).build()
            );
        }
    }


    public Map<LocalDateTime,Long> getPlRatio(Integer aid) {

        List<AccountHistory> accountHistories = accountHistoryRepository.findByAid(aid);
        Map<LocalDateTime,Long> resultMap = new HashMap<>();

        if(!accountHistories.isEmpty()) {
            for(AccountHistory accountHistory : accountHistories) {
                resultMap.put(accountHistory.getDate(),accountHistory.getBalance());
            }
        }

        log.debug(String.format("[AccountService] getPlRatio(aid: %d) -> isSuccess: %b", aid, accountHistories));

        return resultMap;
    }

    public List<AccountResponse> getGameAccount(int uid) {
        List<AccountResponse> accountResponses = accountRepository.findByUserUidAndType(uid, "GAME")
                .stream()
                .map((a) -> AccountResponse.from(a))
                .toList();
        log.debug(String.format("[AccountService] getGameAccount(uid: %d) -> accounts: %s", uid, accountResponses));
        return accountResponses;
    }
}
