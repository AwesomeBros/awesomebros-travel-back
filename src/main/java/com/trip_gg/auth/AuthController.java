package com.trip_gg.auth; // 파일종류: Controller (로그아웃 전용)

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
@RequestMapping("/api/users")
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

            // ✅ 개발환경용 쿠키 옵션 조정
            ResponseCookie accessCookie = ResponseCookie.from("accessToken", accessToken)
                    .httpOnly(true)
                    .secure(false)            // ★ 변경: 로컬 HTTP에서 쿠키 전송되도록
                    .path("/")
                    .maxAge(60 * 60 * 12)
                    .sameSite("Lax")          // ★ 변경: 엄격 모드 → Lax
                    .build();

            ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", refreshToken)
                    .httpOnly(true)
                    .secure(false)            // ★ 변경
                    .path("/")
                    .maxAge(60 * 60 * 24 * 7)
                    .sameSite("Lax")          // ★ 변경
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

    /**
     * 로그아웃
     * - accessToken/refreshToken 쿠키 즉시 만료
     * - 서버 저장소에 보관된 refreshToken 무효화
     */
    @PostMapping("/logout") //
    public ResponseEntity<Map<String, String>> logout(HttpServletRequest request) {

        // 1) 쿠키에서 refreshToken 추출 (필터/프로바이더와 동일한 이름 사용)  :contentReference[oaicite:5]{index=5}
        String refreshToken = null;
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("refreshToken".equals(cookie.getName())) {
                    refreshToken = cookie.getValue();
                    break;
                }
            }
        }

        // 2) 유효한 refreshToken이면 서버 저장소에서 삭제
        if (refreshToken != null && jwtTokenProvider.validateToken(refreshToken)) { // :contentReference[oaicite:6]{index=6}
            String users_id = jwtTokenProvider.getUserIdFromToken(refreshToken);     // :contentReference[oaicite:7]{index=7}
            // 서버 저장(refreshTokenStore)에서 제거
            jwtTokenProvider.delete(users_id); // ✅ [추가] JwtTokenProvider에 구현
        }

        // 3) 쿠키 즉시 만료(발급 시와 동일 옵션)
        ResponseCookie expiredAccess = ResponseCookie.from("accessToken", "")
                .httpOnly(true)
                .secure(false)   // 개발환경: http 기준
                .path("/")
                .sameSite("Lax")
                .maxAge(0)       // 즉시 만료
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

        // ✅ 개발환경용 쿠키 옵션 조정
        ResponseCookie accessCookie = ResponseCookie.from("accessToken", newAccessToken)
                .httpOnly(true)
                .secure(false)            // ★ 변경
                .path("/")
                .maxAge(60 * 60 * 12)
                .sameSite("Lax")          // ★ 변경
                .build();

        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", newRefreshToken)
                .httpOnly(true)
                .secure(false)            // ★ 변경
                .path("/")
                .maxAge(60 * 60 * 24 * 7)
                .sameSite("Lax")          // ★ 변경
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(Collections.singletonMap("accessToken", newAccessToken));
    }
}
