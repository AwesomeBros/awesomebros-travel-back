package com.trip_gg.service;

import com.trip_gg.domain.User;
import com.trip_gg.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    // 회원가입
    public void register(User user) {
        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword);

        // createdAt 시간 세팅
        user.setCreatedAt(LocalDateTime.now());

        // 저장
        userMapper.insertUser(user);
    }

    // 로그인용 유저 조회
    public User findByUsername(String username) {
        return userMapper.findByUsername(username);
    }

    // 로그인 검증 메서드
    public boolean checkLogin(String originPassword, String encodedPassword) {
        return passwordEncoder.matches(originPassword, encodedPassword);
    }
}
