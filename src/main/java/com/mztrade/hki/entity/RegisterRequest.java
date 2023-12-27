package com.mztrade.hki.entity;

public class RegisterRequest {
    private String name;
    private String password;

    public String getName() {
        return name;
    }

    public RegisterRequest setName(String name) {
        this.name = name;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public RegisterRequest setPassword(String password) {
        this.password = password;
        return this;
    }
}
