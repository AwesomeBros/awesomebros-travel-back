package com.trip_gg.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.time.Instant;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String secretKey;

    private Key key;

    // 액세스 토큰 유효시간: 30분
    private final long accessTokenValidTime = 1000L * 60 * 30;

    // 리프레시 토큰 유효시간: 7일
    private final long refreshTokenValidTime = 1000L * 60 * 60 * 24 * 7;

    @PostConstruct
    protected void init() {
        // 🔐 시크릿 키 초기화
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    // ✅ 액세스 토큰 생성
    public String generateToken(String userId) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + accessTokenValidTime);

        return Jwts.builder()
                .setSubject(userId) // users_id 저장
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // ✅ 리프레시 토큰 생성
    public String generateRefreshToken(String userId) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + refreshTokenValidTime);

        return Jwts.builder()
                .setSubject(userId)
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

    // ✅ 요청 헤더에서 Bearer 토큰 추출
    public String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // "Bearer " 제거
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
}