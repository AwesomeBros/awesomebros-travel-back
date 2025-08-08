package com.trip_gg.user;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    // 회원가입
    public void register(UserRequestDto dto) {
        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(dto.getPassword());
        User user = dto.toUser(encodedPassword);
        userMapper.insertUser(user);

//        // 가입 시간 저장
//        user.setCreatedAt(LocalDateTime.now());
//
//        // DB 저장
//        userMapper.insertUser(user);
    }

    // 로그인용 유저 조회
    public User findByUsername(String username) {

//        System.out.println("ID 요청 : " + username);
//        User user = userMapper.findByUsername(username);
//        System.out.println("ID 응답 : " + username);
//        return user;
        return userMapper.findByUsername(username);
    }

    public User findById(String username) {
        return userMapper.findById(username);
    }

    // 로그인 검증 메서드
    public boolean checkLogin(String originPassword, String encodedPassword) {
        return passwordEncoder.matches(originPassword, encodedPassword);
    }

    public boolean existsByUsername(String username) {
        return userMapper.existsByUsername(username);
    }

    public boolean existsByEmail(String email) {
        return userMapper.existsByEmail(email);
    }
}
