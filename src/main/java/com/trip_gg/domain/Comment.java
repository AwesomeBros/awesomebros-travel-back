package com.trip_gg.domain;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Comment {

    private int id;
    private int posts_id;
    private String users_id;
    private String content;
    private LocalDateTime created_at;
}
