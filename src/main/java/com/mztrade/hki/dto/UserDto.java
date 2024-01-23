package com.mztrade.hki.dto;


import com.mztrade.hki.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@AllArgsConstructor
@Builder
public class UserDto {

    private int uid;
    private String name;
    private String password;


    public User toEntity(){
        User user = User.builder()
                .uid(uid)
                .name(name)
                .password(password)
                .build();
        return user;
    }

    public UserDto(String name, String password) {
        this.name = name;
        this.password = password;
    }
}
