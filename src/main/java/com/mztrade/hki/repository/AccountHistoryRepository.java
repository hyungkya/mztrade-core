package com.mztrade.hki.repository;

import com.mztrade.hki.entity.AccountHistory;
import com.mztrade.hki.entity.AccountHistoryId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccountHistoryRepository extends JpaRepository<AccountHistory, AccountHistoryId> {
    AccountHistory save(AccountHistory account);
    List<AccountHistory> findByAid(Integer aid);
}
