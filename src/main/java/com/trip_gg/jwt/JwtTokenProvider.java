package com.trip_gg.jwt;

import com.nimbusds.oauth2.sdk.token.RefreshToken;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String secretKey;

    private Key key;

    // 액세스 토큰 유효시간: 24시간
    private final long accessTokenValidTime = 1000L * 60 * 60 * 24;

    // 리프레시 토큰 유효시간: 7일
    private final long refreshTokenValidTime = 1000L * 60 * 60 * 24 * 7;

    // ✅ 메모리 기반 RefreshToken 저장소 (실제 서비스에서는 Redis나 DB 사용)
    private final Map<String, String> refreshTokenStore = new ConcurrentHashMap<>();

    @PostConstruct
    protected void init() {
        // 🔐 시크릿 키 초기화
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    // ✅ 액세스 토큰 생성
    public String generateToken(String users_id) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + accessTokenValidTime);

        return Jwts.builder()
                .setSubject(users_id)
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }


    // ✅ 리프레시 토큰 생성
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

    // ✅ 토큰에서 users_id 추출
    public String getUserIdFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject(); // users_id가 저장된 곳
    }

    // ✅ 토큰 유효성 검사
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (SecurityException | MalformedJwtException | ExpiredJwtException |
                 UnsupportedJwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public String resolveToken(HttpServletRequest request) {
        // 1. Authorization 헤더에서 Bearer 토큰 추출
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }

        // 2. 쿠키에서 accessToken 찾기
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("accessToken".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }

        return null;
    }


    // ✅ 사용자 ID 확인용 alias
    public String getUsername(String token) {
        return getUserIdFromToken(token);
    }

    // ✅ 액세스 토큰 만료 시간 반환 (에폭 밀리초)
    public long getAccessTokenExpiry() {
        long currentEpochMillis = Instant.now().toEpochMilli();
        long expiryEpochMillis = currentEpochMillis + accessTokenValidTime;
        return expiryEpochMillis;
    }

    // ✅ RefreshToken 저장
    public void save(String users_id, String refreshToken) {
        refreshTokenStore.put(users_id, refreshToken);
    }

    // ✅ RefreshToken 조회
    public String findByUserId(String users_id) {
        return refreshTokenStore.get(users_id);
    }

    // RefreshToken 삭제(로그아웃 시)
    public void delete(String users_id) {
        if (users_id != null) {
            refreshTokenStore.remove(users_id);
        }
    }

    // 쿠키에서 refreshToken 추출 유틸(컨트롤러/필터에서 재사용 가능)
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