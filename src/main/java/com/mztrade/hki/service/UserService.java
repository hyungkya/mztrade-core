package com.mztrade.hki.service;

import com.mztrade.hki.entity.User;
import com.mztrade.hki.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public int createUser(String name, String password) {
        return userRepository.createUser(name, password);

    }

    public boolean login(String name, String password) {
        try {
            Optional<User> user = userRepository.getUser(name);
            if (user.isPresent() && user.get().getPassword().matches(password)) {
                return true;
            }
        } catch (EmptyResultDataAccessException e) {}
        return false;
    }
}
