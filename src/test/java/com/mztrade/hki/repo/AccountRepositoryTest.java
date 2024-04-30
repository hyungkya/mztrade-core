package com.mztrade.hki.repo;

import com.mztrade.hki.Util;
import com.mztrade.hki.entity.Account;
import com.mztrade.hki.entity.AccountHistory;
import com.mztrade.hki.repository.AccountHistoryRepository;
import com.mztrade.hki.repository.AccountRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class AccountRepositoryTest {
    @Autowired
    AccountRepository accountRepository;
    @Autowired
    AccountHistoryRepository accountHistoryRepository;

    @Test
    void saveAndDelete() {
        Account account = accountRepository.save(
                Account.builder()
                        .uid(1)
                        .build()
        );
        System.out.println(account);
        System.out.println(accountRepository.findAll());
        accountRepository.deleteById(account.getAid());
        System.out.println(accountRepository.findAll());
    }

    @Test
    void findByUid() {
        List<Account> accounts = accountRepository.findByUserUid(1);
        System.out.println(accounts);
    }

    @Test
    void historySave() {
        Account account = accountRepository.save(
                Account.builder()
                        .uid(1)
                        .build()
        );
        System.out.println(account);
        AccountHistory accountHistory = accountHistoryRepository.save(
                AccountHistory.builder()
                        .aid(account.getAid())
                        .date(Util.stringToLocalDateTime("20220103"))
                        .balance(35432)
                        .build()
        );
        accountHistoryRepository.save(
                AccountHistory.builder()
                        .aid(account.getAid())
                        .date(Util.stringToLocalDateTime("20220104"))
                        .balance(35432)
                        .build()
        );
        accountHistoryRepository.save(
                AccountHistory.builder()
                        .aid(account.getAid())
                        .date(Util.stringToLocalDateTime("20220104"))
                        .balance(35432)
                        .build()
        );
        List<AccountHistory> accountHistories = accountHistoryRepository.findByAid(account.getAid());
        System.out.println(accountHistories);
    }
}
