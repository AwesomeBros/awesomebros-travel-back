package com.trip_gg.controller;

import com.trip_gg.domain.User;
import com.trip_gg.dto.UserResponseDto;
import com.trip_gg.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // 회원가입
    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody User user) {
        userService.register(user); // 🔄 비밀번호 암호화는 UserService에서 처리
        return ResponseEntity.ok("회원가입 성공");
    }

    // 로그인
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User requestuser) {
        User storedUser = userService.findByUsername(requestuser.getUsername());

        String originPassword = requestuser.getPassword();
        String encodedPassword = storedUser != null ? storedUser.getPassword() : "";

        if (storedUser == null || !userService.checkLogin(originPassword, encodedPassword)) {
            return ResponseEntity.status(401).body("아이디 또는 비밀번호가 잘못되었습니다");
        }

        return ResponseEntity.ok(new UserResponseDto(storedUser));
    }
}
