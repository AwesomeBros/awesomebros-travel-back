package com.trip_gg.controller;

import com.trip_gg.domain.RefreshToken;
import com.trip_gg.domain.User;
import com.trip_gg.dto.UserRequestDto;
import com.trip_gg.jwt.JwtTokenProvider;
import com.trip_gg.service.RefreshTokenService;
import com.trip_gg.service.UserService;
import jakarta.validation.Valid;
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

    // 회원가입
    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody @Valid UserRequestDto dto) {
//        System.out.println("dto: " + dto);
        // 중복 체크
        if (userService.existsByUsername(dto.getUsername())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("이미 사용중인 아이디입니다.");
        }

        if (userService.existsByEmail(dto.getEmail())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("이미 사용중인 이메일입니다.");
        }

        userService.register(dto); // UserRequestDto → User 변환은 Service 내부에서 처리
        return ResponseEntity.ok("회원가입 성공");
    }

    // 로그인
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserRequestDto dto) {
        try {
            User storedUser = userService.findByUsername(dto.getUsername());

            if (storedUser == null || !userService.checkLogin(dto.getPassword(), storedUser.getPassword())) {
                return ResponseEntity.status(401).body("아이디 또는 비밀번호가 잘못되었습니다");
            }

            // 디버깅 로그 출력
            System.out.println("로그인 시도: " + storedUser.getUsername());
            System.out.println("닉네임: " + storedUser.getNickname());
            System.out.println("이메일: " + storedUser.getEmail());
            System.out.println("프로필 URL: " + storedUser.getProfileUrl());
            System.out.println("Provider: " + storedUser.getProvider());

            // AccessToken, RefreshToken 각각 발급
            String accessToken = jwtTokenProvider.generateToken(storedUser.getId());
            String refreshToken = jwtTokenProvider.generateRefreshToken(storedUser.getId());

            // RefreshToken 저장
            refreshTokenService.save(storedUser.getId(), refreshToken);

            // Map으로 사용자 정보 구성 (null-safe)
            Map<String, Object> user = new HashMap<>();
            user.put("id", storedUser.getId());
            user.put("nickname", storedUser.getNickname());
            user.put("username", storedUser.getUsername());
            user.put("email", storedUser.getEmail());
            user.put("role", storedUser.getRole());
            user.put("url", storedUser.getProfileUrl() != null ? storedUser.getProfileUrl() : "");
            user.put("provider", storedUser.getProvider() != null ? storedUser.getProvider() : "credentials");

            // Map으로 토큰 정보 구성
            Map<String, Object> serverTokens = Map.of(
                    "accessToken", accessToken,
                    "refreshToken", refreshToken,
                    "expiresIn", jwtTokenProvider.getAccessTokenExpiry()
            );

            // 최종 응답 Map 구성
            Map<String, Object> response = new HashMap<>();
            response.put("user", user);
            response.put("serverTokens", serverTokens);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("로그인 처리 중 오류 발생:");
            e.printStackTrace(); // 콘솔에 스택트레이스 출력
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 내부 오류");
        }
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
