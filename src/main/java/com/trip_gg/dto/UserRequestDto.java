package com.trip_gg.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;
import com.trip_gg.domain.User;

@Getter
@Setter
public class UserRequestDto {
    private String username;
    private String email;
    private String password;

    // DTO -> Entity 변환
    public User toUser(String encodedPassword){
        User user = new User();
        user.setId(UUID.randomUUID().toString());
        user.setUsername(this.username);
        user.setEmail(this.email);
        user.setPassword(encodedPassword);
        user.setRole("USER");
        user.setCreatedAt(LocalDateTime.now());
        return user;
    }
}
