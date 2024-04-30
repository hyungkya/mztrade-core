package com.mztrade.hki.repository;

import com.mztrade.hki.entity.Account;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountRepository extends JpaRepository<Account, Integer> {
    Account save(Account account);
    List<Account> findByUserUid(Integer uid);
    List<Account> findByUserUidAndType(Integer uid, String type);
}
