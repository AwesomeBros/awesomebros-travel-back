// 파일: src/main/java/com/trip_gg/user/UserController.java   // [Controller 파일]

package com.trip_gg.user;

import com.trip_gg.comment.CommentResponseDto;
import com.trip_gg.jwt.JwtTokenProvider;
import com.trip_gg.post.PostResponseDto;
import com.trip_gg.post.PostService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
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

    // 회원가입
    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody @Valid UserRequestDto dto) {
        // 중복 체크
        if (userService.existsByUsername(dto.getUsername())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("이미 사용중인 아이디입니다.");
        }
        if (userService.existsByEmail(dto.getEmail())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("이미 사용중인 이메일입니다.");
        }
        userService.register(dto);
        return ResponseEntity.ok("회원가입 성공");
    }

    // 프로필 조회(인증 필요): 프론트가 바로 호출하는 엔드포인트
    @GetMapping("/profile")
    public ResponseEntity<?> profile(HttpServletRequest request) {
        // 쿠키/헤더에서 토큰 추출
        String token = jwtTokenProvider.resolveToken(request);
        if (token == null || !jwtTokenProvider.validateToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Collections.singletonMap("error", "인증이 필요합니다."));
        }

        // 토큰에서 사용자 ID 추출
        String users_id = jwtTokenProvider.getUserIdFromToken(token);

        // DB에서 사용자 조회 (서비스에 findById 추가)
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

    @GetMapping("/my-posts")
    public ResponseEntity<?> getMyPosts(HttpServletRequest request) {
        try {
            // 토큰에서 사용자 ID 추출
            String token = jwtTokenProvider.resolveToken(request);
            if (token == null || !jwtTokenProvider.validateToken(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Collections.singletonMap("error", "인증이 필요합니다."));
            }

            String users_id = jwtTokenProvider.getUserIdFromToken(token);

            // 해당 사용자가 작성한 게시글 조회
            List<PostResponseDto> myPosts = postService.getPostsByUserId(users_id);

            return ResponseEntity.ok(myPosts);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", "서버 내부 오류"));
        }
    }

    @GetMapping("/liked-posts")
    public ResponseEntity<?> getLikedPosts(HttpServletRequest request) {
        try {
            String token = jwtTokenProvider.resolveToken(request);
            if (token == null || !jwtTokenProvider.validateToken(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Collections.singletonMap("error", "인증이 필요합니다."));
            }
            String users_id = jwtTokenProvider.getUserIdFromToken(token);

            // ✅ 좋아요한 게시글 목록 조회
            List<PostResponseDto> liked = postService.getLikedPostsByUserId(users_id);
            return ResponseEntity.ok(liked);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", "서버 내부 오류"));
        }
    }

    /**
     * 내가 단 댓글 목록
     */
    @GetMapping("/my-comments")
    public ResponseEntity<?> getMyComments(HttpServletRequest request) {
        try {
            String token = jwtTokenProvider.resolveToken(request);
            if (token == null || !jwtTokenProvider.validateToken(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Collections.singletonMap("error", "인증이 필요합니다."));
            }
            String users_id = jwtTokenProvider.getUserIdFromToken(token);

            // ✅ 내가 단 댓글 목록 조회 (최신순)
            List<CommentResponseDto> comments = postService.getMyComments(users_id);
            return ResponseEntity.ok(comments);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", "서버 내부 오류"));
        }
    }
}
