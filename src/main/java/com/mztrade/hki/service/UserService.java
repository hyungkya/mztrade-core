package com.mztrade.hki.service;

import com.mztrade.hki.dto.UserDto;
import com.mztrade.hki.entity.User;
import com.mztrade.hki.repository.UserRepository;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public boolean checkUserDuplicate(String username) {
        return userRepository.existsByName(username);
    }

    public int saveUser(UserDto userDto) {
        return userRepository.save(userDto.toEntity()).getUid();

    }

    public void login(String username, String password) throws Exception {

        User user = userRepository.findByName(username)
                    .orElseThrow(() -> new Exception("유효한 회원ID가 아닙니다."));

        if (!password.equals(user.getPassword())) {
            throw new Exception(("유효한 회원 패스워드가 아닙니다."));
        }
    }
}
