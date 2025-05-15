package com.travel.demo.domain;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Post {
    private Long id;
    private String title;
    private String content;
    private String country;
    private String region;
    private int viewCount;
    private LocalDateTime createdAt;
}
