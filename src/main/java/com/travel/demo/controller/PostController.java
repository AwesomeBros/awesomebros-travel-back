package com.travel.demo.controller;

import com.travel.demo.dto.PostResponseDto;
import com.travel.demo.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    // 최신 / 인기글 목록
    @GetMapping
    public List<PostResponseDto> getPostsSorted(@RequestParam(defaultValue = "latest") String sort){
        return postService.getSortedPosts(sort);
    }

    // 지역별 목록
    @GetMapping("/region")
    public List<PostResponseDto> getPostsByRegion(@RequestParam String name) {
        return postService.getPostsByRegion(name);
    }
}