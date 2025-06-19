package com.trip_gg.controller;

import com.trip_gg.domain.Post;
import com.trip_gg.dto.PostRequestDto;
import com.trip_gg.dto.PostResponseDto;
import com.trip_gg.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    /* 생성 파트 */
    // 글 작성
    @PostMapping
    public ResponseEntity<String> createPost(@RequestBody PostRequestDto postRequestDto) throws IllegalAccessException {
        postService.createPost(postRequestDto);
        System.out.println("받은 데이터는 : " + postRequestDto);
        return ResponseEntity.ok("글 작성 완료");
    }

    /* 조회 파트 */
    // 최신순 또는 인기순 게시글 목록 조회 (쿼리 파라미터: ?sort=latest | popular)
    @GetMapping
    public List<PostResponseDto> getPostsSorted(@RequestParam("sort") String sort) {
        return postService.getSortedPosts(sort);
    }

    // 지역 기준 게시글 목록 조회 (쿼리 파라미터: ?region=서울 등)
    @GetMapping("/cities")
    public List<PostResponseDto> getPostsByCity(@RequestParam("city") String city) {
        return postService.getPostsByCity(city);
    }

    // 전체 게시글 목록 조회
    @GetMapping("/all")
    public List<Post> getPostList(Model model) {
        List<Post> posts = postService.getAllPosts();
        model.addAttribute("posts", posts);
        return posts;
    }

    // 단일 게시글 상세 조회
    @GetMapping("/{id}")
    public ResponseEntity<PostResponseDto> getPostById(@PathVariable Long id) {
        Post post = postService.getPostById(id);
        return ResponseEntity.ok(PostResponseDto.from(post));
    }

}
