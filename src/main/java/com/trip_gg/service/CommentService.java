package com.trip_gg.service;

import com.trip_gg.domain.Comment;
import com.trip_gg.dto.CommentRequestDto;
import com.trip_gg.dto.CommentResponseDto;
import com.trip_gg.mapper.CommentMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentMapper commentMapper;

    // ✅ 댓글 저장
    @Transactional
    public void createComment(CommentRequestDto dto, String users_id) {
        // 🟡 DTO → 엔티티 변환
        Comment comment = dto.toEntity(users_id);
        // 🟡 현재 시간 삽입
        comment.setCreated_at(LocalDateTime.now());
        // 🟢 댓글 INSERT
        commentMapper.insertComment(comment);
        // 🟢 counts 테이블의 comment_count 증가
        commentMapper.increaseCommentCount(comment.getPosts_id());
    }

    // ✅ 댓글 페이징 조회 + DTO 변환 + 출력
    public List<CommentResponseDto> findCommentsByPostId(int posts_id) {

        // 🟡 원본 Comment 엔티티 리스트 조회
        List<Comment> list = commentMapper.findCommentsByPostId(posts_id);

//        System.out.println("===== 원본 Comment 엔티티 리스트 =====");
        for (Comment c : list) {
            System.out.println(c);
        }

        // 🟢 DTO로 변환
        List<CommentResponseDto> dtos = list.stream()
                .map(CommentResponseDto::from)
                .collect(Collectors.toList());

        // 변환된 DTO 출력
//        System.out.println("===== 변환된 CommentResponseDto 리스트 =====");
        for (CommentResponseDto dto : dtos) {
            System.out.println(dto);
        }

        return dtos;
    }

    // ✅ 총 댓글 수 반환
    public int countComments(int posts_id) {
        return commentMapper.countCommentsByPostId(posts_id);
    }
}
