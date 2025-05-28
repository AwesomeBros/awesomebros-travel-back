package com.trip_gg.service;

import com.trip_gg.dto.PostRequestDto;
import com.trip_gg.dto.PostResponseDto;
import com.trip_gg.mapper.PostMapper;
import com.trip_gg.domain.Post;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostMapper postMapper;

    // 게시글 작성
    public void createPost(PostRequestDto requestDto) throws IllegalAccessException {
        Post post = requestDto.toPost();
        post.setCreatedAt(LocalDateTime.now());

        int isValid = postMapper.checkLocationValidity(
                post.getCountries_id(),
                post.getCities_id(),
                post.getDistricts_id()
        );

        if (isValid == 0) {
            throw new IllegalAccessException("국가, 도시, 지역 선택이 잘못되었습니다.");
        }

        postMapper.insertPost(post);
    }

    // 게시글 최신순/인기순 불러오기
    public List<PostResponseDto> getSortedPosts(String sort) {
        List<Post> posts = sort.equals("popular")
                ? postMapper.findPopularPosts()
                : postMapper.findLatestPosts();

        return posts.stream()
                .map(PostResponseDto::from)
                .collect(Collectors.toList());
    }

    // 게시글 지역별 불러오기
    public List<PostResponseDto> getPostsByRegion(String region) {
        return postMapper.findByRegion(region).stream()
                .map(PostResponseDto::from)
                .collect(Collectors.toList());
    }

    // 게시글 목록 불러오기
    public List<Post> getAllPosts() {
        return postMapper.getAllPosts();
    }

    // 게시글 상세보기
    public Post getPostById(Long id) {
        return postMapper.getPostById(id);
    }
}