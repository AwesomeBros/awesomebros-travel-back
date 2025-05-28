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

    @PostMapping
    public ResponseEntity<String> createPost(@RequestBody PostRequestDto postRequestDto) throws IllegalAccessException {
        postService.createPost(postRequestDto);
        return ResponseEntity.ok("글 작성 완료");
    }

    // 최신 / 인기글 목록
    @GetMapping
    public List<PostResponseDto> getPostsSorted(@RequestParam(defaultValue = "latest") String sort) {
        return postService.getSortedPosts(sort);
    }

    // 지역별 목록
    @GetMapping("/region")
    public List<PostResponseDto> getPostsByRegion(@RequestParam String region) {
        return postService.getPostsByRegion(region);
    }

    @GetMapping("/posts")
    public List<Post> getPostList(Model model) {
        List<Post> posts = postService.getAllPosts();
        model.addAttribute("posts", posts);
        return posts;
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostResponseDto> getPostById(@PathVariable Long id) {
        Post post = postService.getPostById(id);
        return ResponseEntity.ok(PostResponseDto.from(post));
    }

}
