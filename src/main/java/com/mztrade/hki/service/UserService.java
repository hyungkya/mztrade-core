package com.mztrade.hki.service;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import com.mztrade.hki.dto.UserDto;
import com.mztrade.hki.entity.User;
import com.mztrade.hki.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final FirebaseAuth firebaseAuth;

    public int saveUser(UserDto userDto) {
        if (userRepository.findByFirebaseUid(userDto.getFirebaseUid()).isPresent()) {
            throw new RuntimeException("이미 가입된 회원입니다.");
        } else if (userDto.getName().isEmpty()) {
            User user = User.builder()
                    .name(makeRandomName())
                    .firebaseUid(userDto.getFirebaseUid())
                    .role("ROLE_USER").build();
            return userRepository.save(user).getUid();
        } else if (userRepository.findByName(userDto.getName()).isPresent()) {
            throw new RuntimeException("이미 존재하는 닉네임입니다.");
        } else {
            User user = User.builder()
                    .name(userDto.getName())
                    .firebaseUid(userDto.getFirebaseUid())
                    .role("ROLE_USER").build();
            return userRepository.save(user).getUid();
        }
    }
    public String makeRandomName() {
        String randomName;
        do {
            Random r = new Random();
            String randomNumber = "";
            for (int i = 0; i < 6; i++) {
                randomNumber += Integer.toString(r.nextInt(10));
            }
            randomName = "유저" + randomNumber;
        } while (userRepository.existsByName(randomName));
        return randomName;
    }

    public UserDto findUser(String firebaseUid) throws IllegalArgumentException {
        User user = userRepository.findByFirebaseUid(firebaseUid)
                    .orElseThrow(() -> new IllegalArgumentException("유효한 회원ID가 아닙니다."));

        return UserDto.fromEntity(user);
    }

    public boolean isEmailExists(String email) {
        try {
            firebaseAuth.getUserByEmail(email);
            return true;
        } catch (FirebaseAuthException e) {
            return false;
        }
    }

    public boolean isNameExists(String name) {
        Optional<User> user = userRepository.findByName(name);
        if (user.isPresent()) {
            return true;
        } else {
            return false;
        }
    }
}
