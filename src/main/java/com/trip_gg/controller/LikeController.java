package com.trip_gg.controller;

import com.trip_gg.dto.LikeRequestDto;
import com.trip_gg.jwt.JwtTokenProvider;
import com.trip_gg.service.LikeService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.apache.catalina.connector.Request;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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

    @GetMapping("/{posts_id}/count")
    public ResponseEntity<Integer> getLikeCount(@PathVariable int posts_id) {
        int like_count = likeService.getLike_count(posts_id);
        return ResponseEntity.ok(like_count);
    }

    @GetMapping("/{posts_id}/status")
    public ResponseEntity<Boolean> isLiked(@PathVariable int posts_id, HttpServletRequest request) {
        String token = jwtTokenProvider.resolveToken(request);
        String users_id = jwtTokenProvider.getUserIdFromToken(token);

        boolean liked = likeService.isLiked(posts_id, users_id);
        return ResponseEntity.ok(liked);
    }
}
