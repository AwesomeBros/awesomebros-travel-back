package com.trip_gg.domain;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RefreshToken {
   private String userId;
   private String token;
   private String tokenType;
   private LocalDateTime issuedAt;
}
