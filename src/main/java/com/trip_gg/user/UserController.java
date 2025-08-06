package com.trip_gg.user;

import com.trip_gg.jwt.JwtTokenProvider;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
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
    public ResponseEntity<?> login(@RequestBody UserRequestDto dto, HttpServletResponse response) {
        try {
            User storedUser = userService.findByUsername(dto.getUsername());

            if (storedUser == null || !userService.checkLogin(dto.getPassword(), storedUser.getPassword())) {
                return ResponseEntity.status(401).body("아이디 또는 비밀번호가 잘못되었습니다");
            }

            // AccessToken, RefreshToken 발급
            String accessToken = jwtTokenProvider.generateToken(storedUser.getId());
            String refreshToken = jwtTokenProvider.generateRefreshToken(storedUser.getId());

            // RefreshToken 저장 (DB or Redis)
            jwtTokenProvider.save(storedUser.getId(), refreshToken);

            // Access Token 쿠키
            ResponseCookie accessCookie = ResponseCookie.from("accessToken", accessToken)
                    .httpOnly(true)
                    .secure(true)
                    .path("/")
                    .maxAge(60 * 60 * 12) // 12시간
                    .sameSite("Strict")
                    .build();

            // Refresh Token 쿠키
            ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", refreshToken)
                    .httpOnly(true)
                    .secure(true)
                    .path("/")
                    .maxAge(60 * 60 * 24 * 7) // 7일
                    .sameSite("Strict")
                    .build();

            response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
            response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

            // 사용자 정보만 응답
            Map<String, Object> user = new HashMap<>();
            user.put("id", storedUser.getId());
            user.put("nickname", storedUser.getNickname());
            user.put("username", storedUser.getUsername());
            user.put("email", storedUser.getEmail());
            user.put("role", storedUser.getRole());
            user.put("url", storedUser.getProfileUrl() != null ? storedUser.getProfileUrl() : "");
            user.put("provider", storedUser.getProvider() != null ? storedUser.getProvider() : "credentials");

            return ResponseEntity.ok(user);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 내부 오류");
        }
    }


    // 리프레시 토큰으로 Access 토큰 재발급
    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshAccessToken(HttpServletRequest request) {
        String refreshToken = null;

        // 1️⃣ 쿠키에서 refreshToken 찾기
        if (request.getCookies() != null) {
            for (jakarta.servlet.http.Cookie cookie : request.getCookies()) {
                if ("refreshToken".equals(cookie.getName())) {
                    refreshToken = cookie.getValue();
                    break;
                }
            }
        }

        // 2️⃣ 토큰이 없으면 거부
        if (refreshToken == null || !jwtTokenProvider.validateToken(refreshToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("유효하지 않은 리프레시 토큰");
        }

        // 3️⃣ 토큰에서 userId 추출
        String userId = jwtTokenProvider.getUserIdFromToken(refreshToken);

        // 4️⃣ 저장된 토큰과 비교
        String savedToken = jwtTokenProvider.findByUserId(userId);
        if (savedToken == null || !refreshToken.equals(savedToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("저장된 토큰과 일치하지 않음");
        }

        // 5️⃣ AccessToken 새로 발급
        String newAccessToken = jwtTokenProvider.generateToken(userId);

        // 6️⃣ 응답
        return ResponseEntity.ok(Collections.singletonMap("accessToken", newAccessToken));
    }

}
