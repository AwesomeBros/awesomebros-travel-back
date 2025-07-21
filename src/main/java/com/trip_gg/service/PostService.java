package com.trip_gg.service;

import com.trip_gg.domain.Comment;
import com.trip_gg.domain.Count;
import com.trip_gg.domain.Location;
import com.trip_gg.domain.Post;
import com.trip_gg.dto.CommentResponseDto;
import com.trip_gg.dto.CountResponseDto;
import com.trip_gg.dto.LocationDto;
import com.trip_gg.dto.PostRequestDto;
import com.trip_gg.dto.PostResponseDto;
import com.trip_gg.mapper.CountMapper;
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
    private final CountMapper countMapper;

    @Transactional
    public void createPost(PostRequestDto postRequestDto) throws IllegalAccessException, IOException {
        Post post = postRequestDto.toPost();
        post.setCreated_at(LocalDateTime.now());

        int isValid = postMapper.checkLocationValidity(
                post.getCountries_id(), post.getCities_id(), post.getDistricts_id()
        );
        if (isValid == 0) {
            throw new IllegalAccessException("국가, 도시, 지역 선택이 잘못되었습니다.");
        }

        post.setUsers_id(postRequestDto.getUsers_id());
        postMapper.insertPost(post);
        postMapper.upsertCounts(post.getId());

        String originUrl = postRequestDto.getUrl();
        String serverUrl = "http://localhost:8080";
        String finalUrl = null;

        if (originUrl != null && originUrl.contains("/temp/")) {
            finalUrl = moveFileFromTemp(originUrl, post.getId());
            post.setUrl(serverUrl + finalUrl);
        }

        postMapper.update(post);

        List<Location> locations = postRequestDto.toLocation(post.getId());
        for (Location loc : locations) {
            loc.setPosts_id(post.getId());
            locationMapper.insertLocation(loc);
        }
    }

    @Transactional
    public void update(int id, PostRequestDto postRequestDto) throws IOException, IllegalAccessException {
        Post post = postRequestDto.toPost();
        post.setId(id);

        int isValid = postMapper.checkLocationValidity(
                post.getCountries_id(), post.getCities_id(), post.getDistricts_id());
        if (isValid == 0) {
            throw new IllegalAccessException("국가, 도시, 지역 선택이 잘못되었습니다.");
        }

        String originUrl = postRequestDto.getUrl();
        String serverUrl = "http://localhost:8080";
        String finalUrl = null;

        if (originUrl != null && originUrl.contains("/temp/")) {
            finalUrl = moveFileFromTemp(originUrl, id);
            post.setUrl(serverUrl + finalUrl);
        } else {
            post.setUrl(originUrl);
        }

        post.setUsers_id(postRequestDto.getUsers_id());
        postMapper.update(post);
        postMapper.upsertCounts(post.getId());

        locationMapper.deleteLocationByPostId(id);
        List<Location> locations = postRequestDto.toLocation(id);
        for (Location loc : locations) {
            loc.setPosts_id(id);
            locationMapper.insertLocation(loc);
        }
    }

    @Transactional
    private String moveFileFromTemp(String tempUrl, int posts_id) throws IOException {
        if (tempUrl == null) {
            throw new IOException("파일 경로가 null입니다.");
        }

        String fileName = tempUrl.substring(tempUrl.lastIndexOf("/") + 1);
        String tempPath = System.getProperty("user.dir") + "/uploads/temp/" + fileName;
        String destDirPath = System.getProperty("user.dir") + "/uploads/final/" + posts_id;
        String destPath = destDirPath + "/" + fileName;

        File tempFile = new File(tempPath);
        File destDir = new File(destDirPath);
        File destFile = new File(destPath);

        if (!tempFile.exists()) {
            throw new IOException("임시 파일이 존재하지 않습니다: " + tempPath);
        }

        if (!destDir.exists()) destDir.mkdirs();

        if (tempFile.renameTo(destFile)) {
            return "/uploads/final/" + posts_id + "/" + fileName;
        } else {
            throw new IOException("파일 이동 실패");
        }
    }

    public List<PostResponseDto> getSortedPosts(String sort) {
        List<Post> posts = sort.equals("popular")
                ? postMapper.findPopularPosts()
                : postMapper.findLatestPosts();

        return posts.stream()
                .map(PostResponseDto::from)
                .collect(Collectors.toList());
    }

    public List<PostResponseDto> getPostsByCity(String city) {
        return postMapper.getPostsByCity(city).stream()
                .map(PostResponseDto::from)
                .collect(Collectors.toList());
    }

    public List<Post> getAllPosts() {
        return postMapper.getAllPosts();
    }

    public PostResponseDto getPostById(int id) {
        Post post = postMapper.getPostById(id);

        List<Location> locationList = locationMapper.getLocationById(id);
        List<LocationDto> locations = locationList.stream()
                .map(LocationDto::from)
                .collect(Collectors.toList());

        List<Comment> commentList = post.getComments();
        List<CommentResponseDto> comments = commentList.stream()
                .map(CommentResponseDto::from)
                .collect(Collectors.toList());

        Count count = countMapper.getCountByPostId(id);
        List<CountResponseDto> counts = (count != null)
                ?List.of(CountResponseDto.from(count))
                :List.of();

        return PostResponseDto.from(post, locations, comments, counts);
    }
}
