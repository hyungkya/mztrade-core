package com.mztrade.hki.entity;

public class LoginRequest {
    private String name;
    private String password;

    public String getName() {
        return name;
    }

    public LoginRequest setName(String name) {
        this.name = name;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public LoginRequest setPassword(String password) {
        this.password = password;
        return this;
    }
}
