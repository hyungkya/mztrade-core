package com.mztrade.hki.dto;

import com.mztrade.hki.entity.Account;
import com.mztrade.hki.entity.Position;
import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder(toBuilder = true)
public class AccountResponse {
    private int aid;
    private int uid;
    private long balance;
    private String type;
    static public AccountResponse from(Account account) {
        return AccountResponse.builder()
                .aid(account.getAid())
                .uid(account.getUser().getUid())
                .balance(account.getBalance())
                .type(account.getType())
                .build();
    }
}
