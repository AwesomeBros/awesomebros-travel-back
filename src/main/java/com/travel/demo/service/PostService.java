package com.travel.demo.service;

import com.travel.demo.dto.PostResponseDto;
import com.travel.demo.mapper.PostMapper;
import com.travel.demo.domain.Post;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostMapper postMapper;

    public List<PostResponseDto> getSortedPosts(String sort) {
        List<Post> posts = sort.equals("popular")
                ? postMapper.findPopularPosts()
                : postMapper.findLatestPosts();

        return posts.stream()
                .map(PostResponseDto::from)
                .collect(Collectors.toList());
    }

    public List<PostResponseDto> getPostsByRegion(String region) {
        return postMapper.findByRegion(region).stream()
                .map(PostResponseDto::from)
                .collect(Collectors.toList());
    }
}