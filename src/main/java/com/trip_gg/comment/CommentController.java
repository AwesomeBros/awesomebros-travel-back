package com.trip_gg.comment;

import com.trip_gg.jwt.JwtTokenProvider;
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

    /* ✅ 댓글 작성 (프론트의 createComment와 매핑) */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> createComment(@RequestBody CommentRequestDto dto,
                                                HttpServletRequest request) {

        // ✅ JWT 토큰에서 사용자 ID 추출
        String token = jwtTokenProvider.resolveToken(request);
        if (token == null || token.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("JWT 토큰이 유효하지 않습니다.");
        }
        String users_id = jwtTokenProvider.getUserIdFromToken(token);

        commentService.createComment(dto, users_id);

//        System.out.println("댓글 작성 요청: users_id = " + users_id + ", content = " + dto.getContent());
        return ResponseEntity.ok("댓글 작성 완료");
    }

    /* ✅ 댓글 조회 (프론트의 findCommentsByPostId와 매핑) */
    @GetMapping("/posts/{posts_id}")
    public List<CommentResponseDto> getComments(@PathVariable int posts_id,
                                                HttpServletRequest request) {

        // ✅ JWT 토큰에서 사용자 ID 추출 (없어도 오류 발생하지 않도록 처리)
        String token = jwtTokenProvider.resolveToken(request);
        String users_id = "anonymous"; // 기본값

        if (token != null && !token.trim().isEmpty()) {
            users_id = jwtTokenProvider.getUserIdFromToken(token);
        }

//        System.out.println("댓글 조회 요청: users_id = " + users_id + ", posts_id = " + posts_id);

        List<CommentResponseDto> comments = commentService.findCommentsByPostId(posts_id);

//        System.out.println("===== 현재 가져온 댓글 DTO 리스트 =====");
        for (CommentResponseDto dto : comments) {
            System.out.println(dto);
        }

        return comments;
    }
}
