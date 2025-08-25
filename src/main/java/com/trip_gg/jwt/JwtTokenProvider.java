// 파일 유형: Security/Util (JWT Provider)
// 경로 예시: src/main/java/com/trip_gg/jwt/JwtTokenProvider.java
// 역할: 액세스/리프레시 토큰 생성, 검증, 파싱, 저장(임시), 그리고 요청에서 토큰 추출
// 주의: 실제 운영에서는 refreshTokenStore 대신 Redis/DB 사용 권장

package com.trip_gg.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;                 // ✅ 추가: 안전한 인코딩
import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String secretKey;                            // yml에서 주입

    private Key key;                                     // 서명키

    // 액세스 토큰 유효시간: 24시간
    private final long accessTokenValidTime = 1000L * 60 * 60 * 24;

    // 리프레시 토큰 유효시간: 7일
    private final long refreshTokenValidTime = 1000L * 60 * 60 * 24 * 7;

    // ✅ 메모리 기반 RefreshToken 저장소 (실서비스는 Redis/DB 권장)
    private final Map<String, String> refreshTokenStore = new ConcurrentHashMap<>();

    @PostConstruct
    protected void init() {
        // 🔐 시크릿 키 초기화
        // ✅ 수정: 명시적 UTF-8 인코딩 사용(플랫폼 의존성 제거)
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    // ========================
    // 토큰 생성
    // ========================

    /** ✅ 액세스 토큰 생성 (subject: users_id) */
    public String generateToken(String users_id) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + accessTokenValidTime);

        return Jwts.builder()
                .setSubject(users_id)                   // subject에 users_id 저장
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /** ✅ 리프레시 토큰 생성 (subject: users_id) */
    public String generateRefreshToken(String users_id) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + refreshTokenValidTime);

        return Jwts.builder()
                .setSubject(users_id)
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // ========================
    // 토큰 파싱/검증
    // ========================

    /** ✅ 토큰에서 users_id(subject) 추출 */
    public String getUserIdFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .setAllowedClockSkewSeconds(60)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject(); // users_id 저장 위치
    }

    /** ✅ 토큰 유효성 검사 (서명/만료 등) */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .setAllowedClockSkewSeconds(60) // ⬅️ 시계 오차 허용(선택)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e) {
            System.out.println("[JWT] Expired: " + e.getMessage());
        } catch (UnsupportedJwtException e) {
            System.out.println("[JWT] Unsupported: " + e.getMessage());
        } catch (MalformedJwtException e) {
            System.out.println("[JWT] Malformed: " + e.getMessage());
        } catch (SecurityException e) {
            System.out.println("[JWT] Signature invalid: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.out.println("[JWT] Illegal arg: " + e.getMessage());
        }
        return false;
    }

    // ========================
    // 요청에서 토큰 추출
    // ========================

    /**
     * ✅ 토큰 추출(헤더 → 쿠키 순서로 시도)
     * - Authorization: Bearer <token>
     * - Cookie: accessToken=<token>   // 현재 프로젝트에서 쓰는 실제 쿠키명에 맞춤
     */
    public String resolveToken(HttpServletRequest request) {
        // 1) Authorization 헤더에서 Bearer 토큰 추출
        //    Postman 등 클라이언트에서 가장 일반적이고 표준적인 방식
        String bearerToken = request.getHeader("Authorization");
        System.out.println("[DEBUG] Authorization header = " + bearerToken);

        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // "Bearer " 제거
        }

        // 2) 쿠키에서 accessToken 찾기 (프론트가 쿠키로만 주입하는 경우 지원)
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                // ✅ 유지: 현재 코드에 맞춰 "accessToken" 사용 (대소문자/이름 주의)
                if ("accessToken".equals(cookie.getName())) {
                    String v = cookie.getValue();
                    if (v != null && !v.isBlank()) {                    // ✅ 추가: 빈 값 방지
                        return v;
                    }
                }
            }
        }

        // 3) 없으면 null
        return null;
    }

    // ========================
    // 편의/도우미 메서드
    // ========================

    /** ✅ 사용자 ID 확인용 alias (과거 호환) */
    public String getUsername(String token) {
        return getUserIdFromToken(token);
    }

    /** ✅ 액세스 토큰 만료 시간(밀리초 epoch) 반환 */
    public long getAccessTokenExpiry() {
        long currentEpochMillis = Instant.now().toEpochMilli();
        return currentEpochMillis + accessTokenValidTime;
    }

    // ========================
    // Refresh Token 저장/조회/삭제 (임시 구현)
    // ========================

    /** ✅ RefreshToken 저장 (users_id ↔ refreshToken 매핑) */
    public void save(String users_id, String refreshToken) {
        refreshTokenStore.put(users_id, refreshToken);
    }

    /** ✅ RefreshToken 조회 */
    public String findByUserId(String users_id) {
        return refreshTokenStore.get(users_id);
    }

    /** ✅ RefreshToken 삭제(로그아웃 시) */
    public void delete(String users_id) {
        if (users_id != null) {
            refreshTokenStore.remove(users_id);
        }
    }

    /** ✅ 쿠키에서 refreshToken 추출 (필요한 곳에서 사용) */
    public String resolveRefreshToken(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("refreshToken".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}