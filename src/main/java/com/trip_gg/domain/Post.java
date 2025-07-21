package com.trip_gg.domain;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class Post {
    private int id;
    private String title;
    private String content;
    private String users_id;
    private String slug;
    private String url;

    private Long countries_id;
    private Long cities_id;
    private Long districts_id;
    private int posts_id;

    private String name;
    private double lat;
    private double lng;

    private LocalDateTime created_at;

    private List<Comment> comments;
    private List<Location> locations;
    private List<Count> counts;
}
