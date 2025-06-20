package com.trip_gg.domain;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Post {
    private int id;
    private String title;
    private String content;
    private String users_id;
    private String slug;
    private String url;
    private int viewCount;

    private Long countries_id;
    private Long cities_id;
    private Long districts_id;
    private int post_id;

    private LocalDateTime created_at;
}
