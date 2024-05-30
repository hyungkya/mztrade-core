package com.mztrade.hki.repository;

import com.mztrade.hki.entity.AccountHistory;
import com.mztrade.hki.entity.AccountHistoryId;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountHistoryRepository extends JpaRepository<AccountHistory, AccountHistoryId> {
    AccountHistory save(AccountHistory account);
    List<AccountHistory> findByAid(Integer aid);
    Optional<AccountHistory> findByAidAndDate(Integer aid, LocalDateTime date);
}
