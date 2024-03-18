package com.mztrade.hki.repository;

import com.mztrade.hki.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Integer> {
    Account save(Account account);
    List<Account> findByUid(Integer uid);
    List<Account> findByUidAndType(Integer uid, String type);
}
