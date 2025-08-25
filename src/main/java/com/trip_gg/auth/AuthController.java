package com.trip_gg.auth;

import com.trip_gg.jwt.JwtTokenProvider;
import com.trip_gg.user.User;
import com.trip_gg.user.UserRequestDto;
import com.trip_gg.user.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserService userService;

    // 로그인
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserRequestDto dto, HttpServletResponse response) {
        try {
            User storedUser = userService.findByUsername(dto.getUsername());
            if (storedUser == null || !userService.checkLogin(dto.getPassword(), storedUser.getPassword())) {
                return ResponseEntity.status(401).body("아이디 또는 비밀번호가 잘못되었습니다");
            }

            // 토큰 발급
            String accessToken = jwtTokenProvider.generateToken(storedUser.getId());
            String refreshToken = jwtTokenProvider.generateRefreshToken(storedUser.getId());

            // RefreshToken 저장 (메모리/Redis/DB)
            jwtTokenProvider.save(storedUser.getId(), refreshToken);

            // 개발환경용 쿠키 옵션 (HTTP에서도 전송되도록 secure=false, SameSite=Lax)
            ResponseCookie accessCookie = ResponseCookie.from("accessToken", accessToken)
                    .httpOnly(true)
                    .secure(false)
                    .path("/")
                    .maxAge(60 * 60 * 12)
                    .sameSite("Lax")
                    .build();

            ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", refreshToken)
                    .httpOnly(true)
                    .secure(false)
                    .path("/")
                    .maxAge(60 * 60 * 24 * 7)
                    .sameSite("Lax")
                    .build();

            response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
            response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

            // 응답 바디: user + accessToken + refreshToken (Postman Scripts에서 바로 저장 가능)
            Map<String, Object> resp = new HashMap<>();
            resp.put("user", Map.of(
                    "id", storedUser.getId(),
                    "nickname", storedUser.getNickname(),
                    "username", storedUser.getUsername(),
                    "email", storedUser.getEmail(),
                    "role", storedUser.getRole(),
                    "url", storedUser.getProfileUrl() != null ? storedUser.getProfileUrl() : "",
                    "provider", storedUser.getProvider() != null ? storedUser.getProvider() : "credentials"
            ));
            resp.put("accessToken", accessToken);
            resp.put("refreshToken", refreshToken);

            return ResponseEntity.ok(resp);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 내부 오류");
        }
    }

    /**
     * 로그아웃
     * - accessToken/refreshToken 쿠키 즉시 만료
     * - 서버 저장소에 보관된 refreshToken 무효화
     */
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(HttpServletRequest request) {

        // 쿠키에서 refreshToken 추출
        String refreshToken = null;
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("refreshToken".equals(cookie.getName())) {
                    refreshToken = cookie.getValue();
                    break;
                }
            }
        }

        // 유효하면 저장소에서 제거
        if (refreshToken != null && jwtTokenProvider.validateToken(refreshToken)) {
            String users_id = jwtTokenProvider.getUserIdFromToken(refreshToken);
            jwtTokenProvider.delete(users_id);
        }

        // 쿠키 즉시 만료
        ResponseCookie expiredAccess = ResponseCookie.from("accessToken", "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .sameSite("Lax")
                .maxAge(0)
                .build();

        ResponseCookie expiredRefresh = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .sameSite("Lax")
                .maxAge(0)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, expiredAccess.toString())
                .header(HttpHeaders.SET_COOKIE, expiredRefresh.toString())
                .body(Map.of("message", "logged out"));
    }

    // 리프레시 토큰으로 Access 토큰 재발급
    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshAccessToken(HttpServletRequest request) {
        String refreshToken = null;

        if (request.getCookies() != null) {
            for (jakarta.servlet.http.Cookie cookie : request.getCookies()) {
                if ("refreshToken".equals(cookie.getName())) {
                    refreshToken = cookie.getValue();
                    break;
                }
            }
        }

        if (refreshToken == null || !jwtTokenProvider.validateToken(refreshToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("유효하지 않은 리프레시 토큰");
        }

        String users_id = jwtTokenProvider.getUserIdFromToken(refreshToken);
        String savedToken = jwtTokenProvider.findByUserId(users_id);
        if (savedToken == null || !refreshToken.equals(savedToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("저장된 토큰과 일치하지 않음");
        }

        String newAccessToken = jwtTokenProvider.generateToken(users_id);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(users_id);

        jwtTokenProvider.save(users_id, newRefreshToken);

        ResponseCookie accessCookie = ResponseCookie.from("accessToken", newAccessToken)
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(60 * 60 * 12)
                .sameSite("Lax")
                .build();

        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", newRefreshToken)
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(60 * 60 * 24 * 7)
                .sameSite("Lax")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(Collections.singletonMap("accessToken", newAccessToken));
    }
}