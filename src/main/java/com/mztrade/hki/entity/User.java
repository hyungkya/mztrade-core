package com.mztrade.hki.entity;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(toBuilder = true)
public class User {
    private int uid;
    private String name;
    private String password;
}
