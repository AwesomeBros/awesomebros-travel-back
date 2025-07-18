package com.trip_gg.dto;

import com.trip_gg.domain.Comment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Builder
@ToString // ✅ toString 자동 생성
public class CommentResponseDto {

        private int id;
        private String users_id;
        private String content;
        private String nickname;
        private LocalDateTime created_at;

        // ✅ Entity → DTO 변환 메서드
        public static CommentResponseDto from(Comment comment) {
                return CommentResponseDto.builder()
                        .id(comment.getId())
                        .users_id(comment.getUsers_id())
                        .content(comment.getContent())
                        .nickname(comment.getNickname())
                        .created_at(comment.getCreated_at())
                        .build();
        }
}
