package com.trip_gg.service;

import com.trip_gg.domain.Comment;
import com.trip_gg.domain.Location;
import com.trip_gg.domain.Post;
import com.trip_gg.dto.CommentResponseDto;
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

    // ✅ 게시글 작성 + 위치 저장
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

        // 🔽 먼저 post 저장 (id 먼저 확보 필요)
        postMapper.insertPost(post);

        String originUrl = postRequestDto.getUrl();
        String serverUrl = "http://localhost:8080";
        String finalUrl = null;

        // ✅ null 체크 및 temp 경로 확인 후 이동 처리
        if (originUrl != null && originUrl.contains("/temp/")) {
            finalUrl = moveFileFromTemp(originUrl, post.getId());
            post.setUrl(serverUrl + finalUrl); // 서버 경로로 반영
        }

        // 🔽 게시글 다시 update (url 포함해서)
        postMapper.update(post);

        // 🔽 위치 정보 저장
        List<Location> locations = postRequestDto.toLocation(post.getId());
        for (Location loc : locations) {
            loc.setPosts_id(post.getId());
            locationMapper.insertLocation(loc);
        }
    }

    // ✅ 게시글 수정
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

        // null 체크 및 temp 경로 확인 후 이동 처리
        if (originUrl != null && originUrl.contains("/temp/")) {
            finalUrl = moveFileFromTemp(originUrl, id);
            post.setUrl(serverUrl + finalUrl);
        } else {
            post.setUrl(originUrl); // 이미 final 경로이거나 null일 경우 그대로 사용
        }

        post.setUsers_id(postRequestDto.getUsers_id());

        postMapper.update(post);

        locationMapper.deleteLocationByPostId(id);
        List<Location> locations = postRequestDto.toLocation(id);
        for (Location loc : locations) {
            loc.setPosts_id(id);
            locationMapper.insertLocation(loc);
        }
    }

    // ✅ 파일 이동 메서드: postId 별 디렉토리 생성
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

        if (!destDir.exists()) destDir.mkdirs(); // 디렉토리 없으면 생성

        if (tempFile.renameTo(destFile)) {
            return "/uploads/final/" + posts_id + "/" + fileName; // ✅ 상대 경로 반환
        } else {
            throw new IOException("파일 이동 실패");
        }
    }

    // ✅ 게시글 최신순/인기순 불러오기
    public List<PostResponseDto> getSortedPosts(String sort) {
        List<Post> posts = sort.equals("popular")
                ? postMapper.findPopularPosts()
                : postMapper.findLatestPosts();

        return posts.stream()
                .map(PostResponseDto::from)
                .collect(Collectors.toList());
    }

    // ✅ 게시글 지역별 불러오기
    public List<PostResponseDto> getPostsByCity(String city) {
        return postMapper.getPostsByCity(city).stream()
                .map(PostResponseDto::from)
                .collect(Collectors.toList());
    }

    // ✅ 전체 게시글 목록 불러오기
    public List<Post> getAllPosts() {
        return postMapper.getAllPosts();
    }

    // ✅ 게시글 상세보기 (위치 + 댓글 포함)
    public PostResponseDto getPostById(int id) {
        Post post = postMapper.getPostById(id);

        List<Location> locations = locationMapper.getLocationById(id);

//        post.setPosts_id(id);

//        // 🔽 위치 가져오기
//        List<Location> locationList = locationMapper.getLocationById(id);
//        List<LocationDto> locationDtos = locationList.stream()
//                .map(LocationDto::from)
//                .collect(Collectors.toList());
//
//        // 🔽 댓글 가져오기
//        List<Comment> commentList = post.getComments();
//        List<CommentResponseDto> commentDtos = commentList.stream()
//                .map(CommentResponseDto::from)
//                .collect(Collectors.toList());
//
//        // 🔽 디버깅 출력
        System.out.println("=====현재 담고있는 속성1 : " + post + "=====");
        if (post.getLocations() != null) {
            for (Location loc : post.getLocations()) {
                System.out.println("=====[Location 정보] posts_id: " + loc.getPosts_id()
                        + ", name: " + loc.getName()
                        + ", lat: " + loc.getLat()
                        + ", lng: " + loc.getLng()
                );
            }
        } else {
            System.out.println("===== Location 정보가 없습니다. =====");
        }
//        System.out.println("=====현재 담고있는 속성2 : " + post.getPosts_id() + "=====");
//        System.out.println("=====현재 담고있는 속성4 : " + commentDtos + "=====");

        // ✅ 위치 + 댓글 포함된 DTO 반환
        return PostResponseDto.from(post);
    }
}
