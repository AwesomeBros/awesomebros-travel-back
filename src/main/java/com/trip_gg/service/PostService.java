package com.trip_gg.service;

import com.trip_gg.domain.Location;
import com.trip_gg.domain.Post;
import com.trip_gg.dto.LocationDto;
import com.trip_gg.dto.PostRequestDto;
import com.trip_gg.dto.PostResponseDto;
import com.trip_gg.mapper.LocationMapper;
import com.trip_gg.mapper.PostMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
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
    public void createPost(PostRequestDto postRequestDto) throws IllegalAccessException, IOException {
        Post post = postRequestDto.toPost();
        post.setCreated_at(LocalDateTime.now());

        // 유효한 국가/도시/지역 조합인지 확인
        int isValid = postMapper.checkLocationValidity(
                post.getCountries_id(), post.getCities_id(), post.getDistricts_id()
        );
        if (isValid == 0) {
            throw new IllegalAccessException("국가, 도시, 지역 선택이 잘못되었습니다.");
        }

        // temp -> uploads 이동
        String finalUrl = moveFileFromTemp(postRequestDto.getUrl());
        String serverUrl = "http://localhost:8080";
        post.setUrl(serverUrl + finalUrl);
        post.setUsers_id(postRequestDto.getUsers_id());

        // 🔽 게시글 저장
        postMapper.insertPost(post);

        // 🔽 위치 정보 저장
        List<Location> locations = postRequestDto.toLocation(post.getId());
        for (Location loc : locations) {
            loc.setPost_id(post.getId());
            locationMapper.insertLocation(loc);
        }
    }

    @Transactional
    public void update(int id, PostRequestDto postRequestDto) throws IOException, IllegalAccessException{
        Post post = postRequestDto.toPost();
        post.setId(id);

        int isValid = postMapper.checkLocationValidity(post.getCountries_id(), post.getCities_id(), post.getDistricts_id());
        if (isValid == 0) {
            throw new IllegalAccessException("국가, 도시, 지역 선택이 잘못되었습니다.");
        }

        String originUrl = postRequestDto.getUrl();
        String finalUrl = null;
        if (originUrl != null && !originUrl.isBlank()) {
            finalUrl = moveFileFromTemp(originUrl);
        }

        String serverUrl = "http://localhost:8080";
        post.setUrl(finalUrl != null ? serverUrl + finalUrl : null);
        post.setUsers_id(postRequestDto.getUsers_id());

        postMapper.update(post);

        locationMapper.deleteLocationByPostId(id);
        List<Location> locations = postRequestDto.toLocation(id);
        for (Location loc : locations) {
            loc.setPost_id(id);
            locationMapper.insertLocation(loc);
        }
    }

    // temp -> uploads 이동 메서드
    @Transactional
    private String moveFileFromTemp(String tempUrl) throws IOException{
        String fileName = tempUrl.substring(tempUrl.lastIndexOf("/") + 1);
        String tempPath = System.getProperty("user.dir") + "/uploads/temp/" + fileName;
        String destPath = System.getProperty("user.dir") + "/uploads/final/" + fileName;

        File tempFile = new File(tempPath);
        File destFile = new File(destPath);

        if (!tempFile.exists()) {
            throw new IOException("임시 파일이 존재하지 않습니다: " + tempPath);
        }

        File uploadDir = new File(System.getProperty("user.dir") + "/uploads/final/");
        if (!uploadDir.exists()) uploadDir.mkdirs();

        if (tempFile.renameTo(destFile)) {
            return "/uploads/final/" + fileName;
        } else {
            throw new IOException("파일 이동 실패");
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

    // 전체 게시글 목록 불러오기
    public List<Post> getAllPosts() {
        return postMapper.getAllPosts();
    }

    // 게시글 상세보기
    public PostResponseDto getPostById(int id) {
        Post post = postMapper.getPostById(id);

        List<Location> locationList = locationMapper.getLocationByPostId(id);
        List<LocationDto> locationDtos = locationList.stream()
                .map(LocationDto::from)
                .collect(Collectors.toList());

        return PostResponseDto.from(post, locationDtos);
    }


}