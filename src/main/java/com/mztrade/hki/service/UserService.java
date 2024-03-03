package com.mztrade.hki.service;

import com.mztrade.hki.dto.UserDto;
import com.mztrade.hki.entity.User;
import com.mztrade.hki.repository.UserRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public boolean checkUserDuplicate(String username) {
        return userRepository.existsByName(username);
    }

    public int saveUser(UserDto userDto) {

        if(userRepository.findByName(userDto.getName()).isPresent()) {
            throw new RuntimeException("이미 가입된 회원입니다.");
        }else{
            User user = User.builder().name(userDto.getName())
                    .password(passwordEncoder.encode(userDto.getPassword()))
                    .role("ROLE_USER").build();
            return userRepository.save(user).getUid();
        }

    }

    public void login(String username, String password) throws Exception {

        User user = userRepository.findByName(username)
                    .orElseThrow(() -> new Exception("유효한 회원ID가 아닙니다."));

        // 평문과 암호화 비밀번호 비교
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new Exception(("유효한 회원 패스워드가 아닙니다."));
        }

    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        Optional<User> findUser = userRepository.findByName(username);
        if (findUser.isPresent()) {
            return findUser.get();

        } else {
            throw new UsernameNotFoundException("사용자를 찾을 수 없습니다.");
        }
    }
}
