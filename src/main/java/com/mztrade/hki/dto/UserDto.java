package com.mztrade.hki.dto;


import com.mztrade.hki.entity.User;
import lombok.*;

@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserDto {

    private int uid;
    private String name;
    private String password;
    private String role;


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
