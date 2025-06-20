package com.trip_gg.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    private String id;
    private String username;
    private String password;
    private String email;
    private String nickname;
    private String role;
    private String provider;
    private String profileUrl;
    private LocalDateTime created_at;
}
