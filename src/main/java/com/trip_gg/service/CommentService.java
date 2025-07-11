package com.trip_gg.service;

import com.trip_gg.domain.Comment;
import com.trip_gg.dto.CommentRequestDto;
import com.trip_gg.dto.CommentResponseDto;
import com.trip_gg.mapper.CommentMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentMapper commentMapper;
    private static final int PAGE_SIZE = 10;

    @Transactional
    public void createComment(CommentRequestDto dto, String users_id) {
        Comment comment = dto.toEntity(users_id);
        comment.setCreated_at(LocalDateTime.now());
        commentMapper.insertComment(comment);
    }

    public List<CommentResponseDto> findCommentsByPostId(int posts_id, int pageParam) {
        int offset = pageParam * PAGE_SIZE;
        List<Comment> list = commentMapper.findCommentsByPostId(posts_id, offset, PAGE_SIZE);
        return list.stream()
                .map(CommentResponseDto::from)
                .collect(Collectors.toList());
    }

    public int countComments(int posts_id) {
        return commentMapper.countCommentsByPostId(posts_id);
    }

}
