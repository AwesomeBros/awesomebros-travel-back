package com.trip_gg.controller;

import com.trip_gg.dto.CommentRequestDto;
import com.trip_gg.dto.CommentResponseDto;
import com.trip_gg.jwt.JwtTokenProvider;
import com.trip_gg.service.CommentService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;
    private final JwtTokenProvider jwtTokenProvider;

    /* 댓글 작성 (프론트의 createComment와 매핑) */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> createComment(@RequestBody CommentRequestDto dto,
                                                HttpServletRequest request) {
        String token = jwtTokenProvider.resolveToken(request);
        String users_id = jwtTokenProvider.getUserIdFromToken(token);
        commentService.createComment(dto, users_id);
        System.out.println("댓글 작성 요청: users_id = " + users_id + ", content = " + dto.getContent());
        return ResponseEntity.ok("댓글 작성 완료");
    }

    /* 댓글 조회 (프론트의 findCommentsByPostId와 매핑) */
    @GetMapping("/{posts_id}")
    public List<CommentResponseDto> getComments(@PathVariable int posts_id,
                                                @RequestParam(defaultValue = "0") int pageParam) {
        return commentService.findCommentsByPostId(posts_id, pageParam);
    }
}
