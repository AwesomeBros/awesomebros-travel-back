package com.trip_gg.controller;

import com.trip_gg.domain.RefreshToken;
import com.trip_gg.domain.User;
import com.trip_gg.dto.UserRequestDto;
import com.trip_gg.dto.UserResponseDto;
import com.trip_gg.jwt.JwtTokenProvider;
import com.trip_gg.service.RefreshTokenService;
import com.trip_gg.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final RefreshTokenService refreshTokenService;
    private final JwtTokenProvider jwtTokenProvider;

    //  회원가입
    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody UserRequestDto dto) {
        userService.register(dto); //  UserRequestDto → User 변환은 Service 내부에서 처리
        return ResponseEntity.ok("회원가입 성공");
    }

    //  로그인
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserRequestDto dto) {
        User storedUser = userService.findByUsername(dto.getUsername());

        if (storedUser == null || !userService.checkLogin(dto.getPassword(), storedUser.getPassword())) {
            return ResponseEntity.status(401).body("아이디 또는 비밀번호가 잘못되었습니다");
        }

        // AccessToken, RefreshToken 각각 발급
        String accessToken = jwtTokenProvider.generateToken(storedUser.getUsername());
        String refreshToken = jwtTokenProvider.generateRefreshToken(storedUser.getUsername());

        // RefreshToken 저장
        refreshTokenService.save(storedUser.getUsername(), refreshToken);

        // 응답 반환 (Map 형태로)
        Map<String, String> tokens = new HashMap<>();
        tokens.put("accessToken", accessToken);
        tokens.put("refreshToken", refreshToken);

        return ResponseEntity.ok(tokens);
    }

    // 리프레시 토큰으로 Access 토큰 재발급
    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshAccessToken(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");

        if (!jwtTokenProvider.validateToken(refreshToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("유효하지 않은 리프레시 토큰");
        }

        String userId = jwtTokenProvider.getUserIdFromToken(refreshToken);
        RefreshToken savedToken = refreshTokenService.findByUserId(userId);

        if (savedToken == null || !refreshToken.equals(savedToken.getToken())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("저장된 토큰과 일치하지 않음");
        }

        String newAccessToken = jwtTokenProvider.generateToken(userId);
        return ResponseEntity.ok(Collections.singletonMap("accessToken", newAccessToken));
    }
}