package com.trip_gg.user;

import com.trip_gg.comment.CommentResponseDto;
import com.trip_gg.jwt.JwtTokenProvider;
import com.trip_gg.post.PostRequestDto;
import com.trip_gg.post.PostResponseDto;
import com.trip_gg.post.PostService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final PostService postService;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 회원가입
     */
    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody @Valid UserRequestDto dto) {
        if (userService.existsByUsername(dto.getUsername())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("이미 사용중인 아이디입니다.");
        }
        if (userService.existsByEmail(dto.getEmail())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("이미 사용중인 이메일입니다.");
        }
        userService.register(dto);
        return ResponseEntity.ok("회원가입 성공");
    }

    /**
     * 프로필 조회 (JWT 인증 필요)
     */
    @GetMapping("/profile")
    public ResponseEntity<?> profile(HttpServletRequest request) {
        String token = jwtTokenProvider.resolveToken(request);
        if (token == null || !jwtTokenProvider.validateToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Collections.singletonMap("error", "인증이 필요합니다."));
        }

        String users_id = jwtTokenProvider.getUserIdFromToken(token);
        User user = userService.findById(users_id);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Collections.singletonMap("error", "유효하지 않은 사용자입니다."));
        }

        Map<String, Object> body = new HashMap<>();
        body.put("id", user.getId());
        body.put("username", user.getUsername());
        body.put("nickname", user.getNickname());
        body.put("email", user.getEmail());
        body.put("role", user.getRole());
        body.put("url", user.getProfileUrl() != null ? user.getProfileUrl() : "");
        body.put("provider", user.getProvider() != null ? user.getProvider() : "credentials");

        return ResponseEntity.ok(body);
    }

    // -----------------------------------------------------------
    // 📌 내가 쓴 게시글 (my-posts)
    // -----------------------------------------------------------

    // 조회
    @GetMapping("/my-posts")
    public ResponseEntity<?> getMyPosts(HttpServletRequest request) {
        try {
            String users_id = validateAndGetUserId(request);
            List<PostResponseDto> myPosts = postService.getPostsByUserId(users_id);
            return ResponseEntity.ok(myPosts);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Collections.singletonMap("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.singletonMap("error", "서버 내부 오류"));
        }
    }

    // 수정
    @PutMapping("/my-posts/{id}")
    public ResponseEntity<?> updateMyPost(@PathVariable int id,
                                          @RequestBody @Valid PostRequestDto dto,
                                          HttpServletRequest request) {
        try {
            String users_id = validateAndGetUserId(request);
            postService.updatePostByOwner(users_id, id, dto);
            return ResponseEntity.ok(Collections.singletonMap("message", "수정 완료"));
        } catch (IllegalAccessException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Collections.singletonMap("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.singletonMap("error", "서버 내부 오류"));
        }
    }

    /*// 삭제
    @DeleteMapping("/my-posts/{id}")
    public ResponseEntity<?> deleteMyPost(@PathVariable int id, HttpServletRequest request) {
        try {
            String users_id = validateAndGetUserId(request);
            postService.deletePostByOwner(users_id, id);
            return ResponseEntity.ok(Collections.singletonMap("message", "삭제 완료"));
        } catch (IllegalAccessException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Collections.singletonMap("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.singletonMap("error", "서버 내부 오류"));
        }
    }*/

    // -----------------------------------------------------------
    // 📌 좋아요 한 게시글 (liked-posts)
    // -----------------------------------------------------------

    @GetMapping("/liked-posts")
    public ResponseEntity<?> getLikedPosts(HttpServletRequest request) {
        try {
            String users_id = validateAndGetUserId(request);
            List<PostResponseDto> liked = postService.getLikedPostsByUserId(users_id);
            return ResponseEntity.ok(liked);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Collections.singletonMap("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.singletonMap("error", "서버 내부 오류"));
        }
    }

    // -----------------------------------------------------------
    // 📌 내가 단 댓글 (my-comments)
    // -----------------------------------------------------------

    // 조회
    @GetMapping("/my-comments")
    public ResponseEntity<?> getMyComments(HttpServletRequest request) {
        try {
            String users_id = validateAndGetUserId(request);
            List<CommentResponseDto> comments = postService.getMyComments(users_id);
            return ResponseEntity.ok(comments);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Collections.singletonMap("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.singletonMap("error", "서버 내부 오류"));
        }
    }

//    // 수정
//    @PutMapping("/my-comments/{comments_id}")
//    public ResponseEntity<?> updateMyComment(@PathVariable int comments_id,
//                                             @RequestBody Map<String, String> body,
//                                             HttpServletRequest request) {
//        try {
//            String users_id = validateAndGetUserId(request);
//            String content = body.getOrDefault("content", "").trim();
//            if (content.isEmpty()) {
//                return ResponseEntity.badRequest().body(Collections.singletonMap("error", "content는 비어 있을 수 없습니다."));
//            }
//            postService.updateMyComment(users_id, comments_id, content);
//            return ResponseEntity.ok(Collections.singletonMap("message", "댓글 수정 완료"));
//        } catch (IllegalAccessException e) {
//            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Collections.singletonMap("error", e.getMessage()));
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.singletonMap("error", "서버 내부 오류"));
//        }
//    }
//
//    // 삭제
//    @DeleteMapping("/my-comments/{comments_id}")
//    public ResponseEntity<?> deleteMyComment(@PathVariable int comments_id, HttpServletRequest request) {
//        try {
//            String users_id = validateAndGetUserId(request);
//            postService.deleteMyComment(users_id, comments_id);
//            return ResponseEntity.ok(Collections.singletonMap("message", "댓글 삭제 완료"));
//        } catch (IllegalAccessException e) {
//            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Collections.singletonMap("error", e.getMessage()));
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.singletonMap("error", "서버 내부 오류"));
//        }
//    }

    // -----------------------------------------------------------
    // 📌 내부 유틸 메서드 (JWT 검증 및 사용자 ID 추출)
    // -----------------------------------------------------------
    private String validateAndGetUserId(HttpServletRequest request) {
        String token = jwtTokenProvider.resolveToken(request);
        if (token == null || !jwtTokenProvider.validateToken(token)) {
            throw new SecurityException("인증이 필요합니다.");
        }
        return jwtTokenProvider.getUserIdFromToken(token);
    }
}
