package com.trip_gg.mapper;

import com.trip_gg.domain.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper {
    // 사용자 저장
    void insertUser(User user);

    boolean existsByEmail(String email);

    // 사용자 조회(로그인용)
    User findByUsername(String username);

}
