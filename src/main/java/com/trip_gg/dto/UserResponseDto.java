package com.trip_gg.dto;

import com.trip_gg.domain.User;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class UserResponseDto {
    private final String id;
    private final String username;
    private final String email;
    private final String nickname;
    private final String role;
    private final String provider;
    private final String url;
    private final LocalDateTime created_at;

    public UserResponseDto(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.nickname = user.getNickname();
        this.role = user.getRole();
        this.provider = user.getProvider();
        this.url = user.getProfileUrl();
        this.created_at = user.getCreated_at();
    }
}
