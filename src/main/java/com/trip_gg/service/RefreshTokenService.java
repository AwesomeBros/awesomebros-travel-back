package com.trip_gg.service;

import com.trip_gg.domain.RefreshToken;
import com.trip_gg.mapper.RefreshTokenMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenMapper refreshTokenMapper;

    public void save(String userId, String token) {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUserId(userId);
        refreshToken.setToken(token);
        refreshToken.setTokenType("refresh"); // 테이블에 새로 추가된 컬럼
        refreshToken.setIssuedAt(LocalDateTime.now()); // 생성 시간 기록
        refreshTokenMapper.save(refreshToken);
    }

    public RefreshToken findByUserId(String userId) {
        return refreshTokenMapper.findByUserId(userId);
    }
}