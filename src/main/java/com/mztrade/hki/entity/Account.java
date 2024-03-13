package com.mztrade.hki.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder(toBuilder = true)
@ToString
public class Account {
    private int aid;
    private int uid;
    private long balance;
    private String type;
}
