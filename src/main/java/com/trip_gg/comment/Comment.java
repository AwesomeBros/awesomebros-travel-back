package com.trip_gg.comment;

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
    private String nickname;
    private LocalDateTime created_at;
}
