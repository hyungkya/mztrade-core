package com.mztrade.hki.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Builder(toBuilder = true)
@ToString
public class AccountHistory {
    private int aid;
    private LocalDateTime date;
    private long balance;
}
