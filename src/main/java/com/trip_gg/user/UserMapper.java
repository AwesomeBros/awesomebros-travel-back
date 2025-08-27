package com.trip_gg.user;

import com.trip_gg.user.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper {
    // 사용자 저장
    void insertUser(User user);

    // 사용자 조회(로그인용)
    User findByUsername(String username);

    User findById(String username);

    // 아이디 중복 검사
    boolean existsByUsername(String username);

    // 이메일 중복 검사
    boolean existsByEmail(String email);

    // 이메일 중복 검사
    boolean existsByEmailExceptUser(@Param("email") String email, @Param("id") String users_id);

    // 닉네임 중복 검사
    boolean existsByNicknameExceptUser(@Param("nickname") String nickname, @Param("id") String users_id);

    // 프로필 수정
    int updateById(User user);

    // 비밀번호 수정
    int updatePassword(@Param("id") String users_id, @Param("password") String password);

    int deleteUser(@Param("id") String users_id);
}
