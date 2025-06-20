package com.trip_gg.service;

import com.trip_gg.domain.Location;
import com.trip_gg.dto.LocationDto;
import com.trip_gg.dto.PostRequestDto;
import com.trip_gg.dto.PostResponseDto;
import com.trip_gg.mapper.LocationMapper;
import com.trip_gg.mapper.PostMapper;
import com.trip_gg.domain.Post;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostMapper postMapper;
    private final LocationMapper locationMapper;

    // 게시글 작성 + 위치 저장
    @Transactional
    public void createPost(PostRequestDto requestDto) throws IllegalAccessException {
        // 1. DTO -> Post 변환
        Post post = requestDto.toPost();
        post.setCreated_at(LocalDateTime.now());
        System.out.println("[DEBUG] 변환된 Post : " + post);

        // 2. 국가-도시-지역 유효성 검사
        int isValid = postMapper.checkLocationValidity(
                post.getCountries_id(),
                post.getCities_id(),
                post.getDistricts_id()
        );

        System.out.println("[DEBUG] 위치 유효성 검사 결과: " + isValid);

        if (isValid == 0) {
            throw new IllegalAccessException("국가, 도시, 지역 선택이 잘못되었습니다.");
        }

        // 3. 게시글 저장
        postMapper.insertPost(post);
        System.out.println("[DEBUG] Post 저장 완료 - ID: " + post.getId());

        // 4. Location 객체 생성 후 저장
        List<Location> locations = requestDto.toLocation(post.getId());
        for (Location loc : locations) {
            loc.setPost_id(post.getId());
            locationMapper.insertLocation(loc);
            System.out.println("[DEBUG] 저장된 Location : " + loc);
        }

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
    public List<PostResponseDto> getPostsByCity(String city) {
        return postMapper.getPostsByCity(city).stream()
                .map(PostResponseDto::from)
                .collect(Collectors.toList());
    }

    // 게시글 목록 불러오기
    public List<Post> getAllPosts() {
        return postMapper.getAllPosts();
    }

    // 게시글 상세보기
    public PostResponseDto getPostById(int id) {
        Post post = postMapper.getPostById(id);

        List<Location> locationList = locationMapper.getLocationByPostId(id); // ← 기존 코드 없음
        List<LocationDto> locationDtos = locationList.stream()
                .map(LocationDto::from)
                .collect(Collectors.toList());

        return PostResponseDto.from(post, locationDtos);
    }
}
