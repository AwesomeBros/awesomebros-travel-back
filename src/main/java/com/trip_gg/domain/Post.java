package com.trip_gg.domain;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Post {
    private Long id;
    private String title;
    private String content;
    private String country;
    private String region;
    private String imageUrl;
    private int viewCount;
    private LocalDateTime createdAt;
}
