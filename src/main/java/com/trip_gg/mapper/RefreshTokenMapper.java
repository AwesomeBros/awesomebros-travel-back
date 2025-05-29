package com.trip_gg.mapper;

import com.trip_gg.domain.RefreshToken;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface RefreshTokenMapper {
    void save(RefreshToken refreshToken);

    RefreshToken findByUserId(String userId);
}