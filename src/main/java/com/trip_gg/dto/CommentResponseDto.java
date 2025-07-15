package com.trip_gg.dto;

import com.trip_gg.domain.Comment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class CommentResponseDto {

        private int id;
        private String users_id;
        private String content;
        private LocalDateTime created_at;

        public static CommentResponseDto from(Comment comment) {
                return CommentResponseDto.builder()
                        .id(comment.getId())
                        .users_id(comment.getUsers_id())
                        .content(comment.getContent())
                        .created_at(comment.getCreated_at())
                        .build();
        }
}
