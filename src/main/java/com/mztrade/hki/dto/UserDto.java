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
@NoArgsConstructor
@Builder
public class UserDto {
    private int uid;
    private String name;
    private String firebaseUid;
    private String role;

    public static UserDto fromEntity(User user) {
        return UserDto.builder()
                .name(user.getName())
                .firebaseUid(user.getFirebaseUid())
                .uid(user.getUid())
                .build();
    }
}
