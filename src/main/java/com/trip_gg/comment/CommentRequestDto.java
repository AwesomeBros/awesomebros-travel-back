package com.trip_gg.comment;

import com.trip_gg.comment.Comment;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CommentRequestDto {

    private String content;
    private int posts_id;

    public Comment toEntity(String users_id) {
        return Comment.builder()
                .posts_id(posts_id)
                .users_id(users_id)
                .content(content)
                .created_at(LocalDateTime.now())
                .build();
    }
}
