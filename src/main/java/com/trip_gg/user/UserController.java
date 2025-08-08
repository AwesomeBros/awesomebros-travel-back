// 파일: src/main/java/com/trip_gg/user/UserController.java   // [Controller 파일]

package com.trip_gg.user;

import com.trip_gg.jwt.JwtTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
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
public class UserController {

    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;

    // 회원가입
    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody @Valid UserRequestDto dto) {
        // 중복 체크
        if (userService.existsByUsername(dto.getUsername())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("이미 사용중인 아이디입니다.");
        }
        if (userService.existsByEmail(dto.getEmail())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("이미 사용중인 이메일입니다.");
        }
        userService.register(dto);
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

    // ✅ 프로필 조회(인증 필요): 프론트가 바로 호출하는 엔드포인트
    @GetMapping("/profile")
    public ResponseEntity<?> profile(HttpServletRequest request) {
        // 쿠키/헤더에서 토큰 추출
        String token = jwtTokenProvider.resolveToken(request);
        if (token == null || !jwtTokenProvider.validateToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Collections.singletonMap("error", "인증이 필요합니다."));
        }

        // 토큰에서 사용자 ID 추출
        String usersId = jwtTokenProvider.getUserIdFromToken(token);

        // DB에서 사용자 조회 (서비스에 findById 추가)
        User user = userService.findById(usersId);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Collections.singletonMap("error", "유효하지 않은 사용자입니다."));
        }

        Map<String, Object> body = new HashMap<>();
        body.put("id", user.getId());
        body.put("username", user.getUsername());
        body.put("nickname", user.getNickname());
        body.put("email", user.getEmail());
        body.put("role", user.getRole());
        body.put("url", user.getProfileUrl() != null ? user.getProfileUrl() : "");
        body.put("provider", user.getProvider() != null ? user.getProvider() : "credentials");

        return ResponseEntity.ok(body);
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
