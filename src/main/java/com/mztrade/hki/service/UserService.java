package com.mztrade.hki.service;

import com.mztrade.hki.dto.UserDto;
import com.mztrade.hki.entity.User;
import com.mztrade.hki.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;

    public int saveUser(UserDto userDto) {
        if(userRepository.findByFirebaseUid(userDto.getFirebaseUid()).isPresent()) {
            throw new RuntimeException("이미 가입된 회원입니다.");
        } else if(userRepository.findByName(userDto.getName()).isPresent()) {
            throw new RuntimeException("이미 존재하는 닉네임입니다.");
        } else{
            User user = User.builder()
                    .name(userDto.getName())
                    .firebaseUid(userDto.getFirebaseUid())
                    .role("ROLE_USER").build();
            return userRepository.save(user).getUid();
        }
    }

    public UserDto findUser(String firebaseUid) throws UsernameNotFoundException {
        User user = userRepository.findByFirebaseUid(firebaseUid)
                    .orElseThrow(() -> new UsernameNotFoundException("유효한 회원ID가 아닙니다."));

        return UserDto.fromEntity(user);
    }
}
