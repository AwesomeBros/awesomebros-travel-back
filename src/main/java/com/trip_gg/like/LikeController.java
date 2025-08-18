package com.trip_gg.like;

import com.trip_gg.jwt.JwtTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/likes")
@RequiredArgsConstructor
public class LikeController {

    private final LikeService likeService;
    private final JwtTokenProvider jwtTokenProvider;

    // ✅ 좋아요 토글 API
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> toggleLike(@RequestBody LikeRequestDto requestDto, HttpServletRequest servletRequest) {

        String token = jwtTokenProvider.resolveToken(servletRequest);
        String users_id = jwtTokenProvider.getUserIdFromToken(token);

        // 인스턴스 변수 likeService 사용
        likeService.toggleLike(requestDto.getPosts_id(), users_id);

        return ResponseEntity.ok("ok");
    }

    @GetMapping
    public ResponseEntity<Integer> isLiked(@RequestParam("posts_id") int posts_id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String users_id = (authentication != null && authentication.isAuthenticated())
                ?(String) authentication.getPrincipal()
                : null;
        Integer isLiked = likeService.isLiked(posts_id, users_id);
        return ResponseEntity.ok(isLiked);
    }

    @GetMapping("/{posts_id}")
    public ResponseEntity<Integer> getLikeCount(@PathVariable int posts_id) {
        int like_count = likeService.getLike_count(posts_id);
        return ResponseEntity.ok(like_count);
    }

}
