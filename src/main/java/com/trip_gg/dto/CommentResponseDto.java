package com.trip_gg.dto;

import com.trip_gg.domain.Comment;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class CommentResponseDto {

        private int id;
        private String users_id;
        private String content;
        private LocalDateTime created_at;

        public static CommentResponseDto from(Comment comment) {
                return new CommentResponseDto(
                        comment.getId(),
                        comment.getUsers_id(),
                        comment.getContent(),
                        comment.getCreated_at()
                );
        }
}
