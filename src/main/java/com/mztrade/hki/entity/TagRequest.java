package com.mztrade.hki.entity;

import lombok.Data;

@Data
public class TagRequest {
    private int uid;
    private String name;
    private String color;
    private String category;
}
