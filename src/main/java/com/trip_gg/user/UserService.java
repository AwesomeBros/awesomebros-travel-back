package com.trip_gg.user;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.jdbc.Null;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    // 회원가입
    public void register(UserRequestDto dto) {
        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(dto.getPassword());
        User user = dto.toUser(encodedPassword);
        userMapper.insertUser(user);

//        // 가입 시간 저장
//        user.setCreatedAt(LocalDateTime.now());
//
//        // DB 저장
//        userMapper.insertUser(user);
    }

    // 로그인용 유저 조회
    public User findByUsername(String username) {

//        System.out.println("ID 요청 : " + username);
//        User user = userMapper.findByUsername(username);
//        System.out.println("ID 응답 : " + username);
//        return user;
        return userMapper.findByUsername(username);
    }

    public User findById(String username) {
        return userMapper.findById(username);
    }

    // 로그인 검증 메서드
    public boolean checkLogin(String originPassword, String encodedPassword) {
        return passwordEncoder.matches(originPassword, encodedPassword);
    }

    public boolean existsByUsername(String username) {
        return userMapper.existsByUsername(username);
    }

    public boolean existsByEmail(String email) {
        return userMapper.existsByEmail(email);
    }


    @Transactional
    public UserResponseDto updateProfile(String userId, UserRequestDto dto)
            throws IllegalAccessException, IllegalArgumentException {

        // 1) 사용자 존재 여부 확인
        User existingUser = userMapper.findById(userId);
        if (existingUser == null) {
            throw new IllegalAccessException("존재하지 않는 사용자입니다.");
        }

        // 2) 이메일 중복 검사 (자기 자신 제외)
        if (!existingUser.getEmail().equals(dto.getEmail()) &&
                userMapper.existsByEmailExceptUser(dto.getEmail(), userId)) {
            throw new IllegalArgumentException("이미 사용중인 이메일입니다.");
        }

        // 3) 닉네임 중복 검사 (자기 자신 제외)
        if (!existingUser.getNickname().equals(dto.getNickname()) &&
                userMapper.existsByNicknameExceptUser(dto.getNickname(), userId)) {
            throw new IllegalArgumentException("이미 사용중인 닉네임입니다.");
        }

        // 4) 사용자 정보 업데이트
        existingUser.setNickname(dto.getNickname().trim());
        existingUser.setEmail(dto.getEmail().trim());

        // 프로필 URL이 제공된 경우에만 업데이트
        if (dto.getProfileUrl() != null && !dto.getProfileUrl().trim().isEmpty()) {
            existingUser.setProfileUrl(dto.getProfileUrl().trim());
        }

        // 5) DB 업데이트
        int updatedRows = userMapper.updateById(existingUser);
        if (updatedRows == 0) {
            throw new IllegalStateException("프로필 업데이트에 실패했습니다.");
        }

        // 6) 업데이트된 사용자 정보 반환
        User updatedUser = userMapper.findById(userId);
        return new UserResponseDto(updatedUser);
    }

    /**
     * 패스워드 변경
     */
    @Transactional
    public void updatePassword(String userId, String currentPassword, String newPassword)
            throws IllegalAccessException, IllegalArgumentException {

        User user = userMapper.findById(userId);
        if (user == null) {
            throw new IllegalAccessException("존재하지 않는 사용자입니다.");
        }

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new IllegalArgumentException("현재 비밀번호가 올바르지 않습니다.");
        }

        String encodedNewPassword = passwordEncoder.encode(newPassword);
        user.setPassword(encodedNewPassword);

        int updatedRows = userMapper.updateById(user);
        if (updatedRows == 0) {
            throw new IllegalStateException("비밀번호 변경에 실패했습니다.");
        }
    }

    @Transactional
    public void deleteUser(String users_id) throws IllegalAccessException {
        User user = userMapper.findById(users_id);
        if (user == null) {
            throw new IllegalAccessException("존재하지 않는 사용자입니다.");
        }

        int updateRows = userMapper.deleteUser(users_id);
        if (updateRows == 0) {
            throw new IllegalAccessException("회원 탈퇴 처리에 실패했습니다.");
        }
    }
}