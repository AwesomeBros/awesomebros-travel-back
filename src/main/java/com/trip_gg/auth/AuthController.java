package com.trip_gg.auth; // 파일종류: Controller (로그아웃 전용)

import com.trip_gg.jwt.JwtTokenProvider;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class AuthController {

    private final JwtTokenProvider jwtTokenProvider;

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
            String usersId = jwtTokenProvider.getUserIdFromToken(refreshToken);     // :contentReference[oaicite:7]{index=7}
            // 서버 저장(refreshTokenStore)에서 제거
            jwtTokenProvider.delete(usersId); // ✅ [추가] JwtTokenProvider에 구현
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
}
