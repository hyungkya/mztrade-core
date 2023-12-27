package com.mztrade.hki.entity;

public class User {
    private int uid;
    private String name;
    private String password;

    public int getUid() {
        return uid;
    }

    public User setUid(int uid) {
        this.uid = uid;
        return this;
    }

    public String getName() {
        return name;
    }

    public User setName(String name) {
        this.name = name;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public User setPassword(String password) {
        this.password = password;
        return this;
    }
}
