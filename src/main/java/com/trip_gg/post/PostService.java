package com.trip_gg.post;

import com.trip_gg.comment.Comment;
import com.trip_gg.count.Count;
import com.trip_gg.like.LikeMapper;
import com.trip_gg.location.Location;
import com.trip_gg.comment.CommentResponseDto;
import com.trip_gg.count.CountResponseDto;
import com.trip_gg.location.LocationDto;
import com.trip_gg.count.CountMapper;
import com.trip_gg.location.LocationMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostMapper postMapper;
    private final LocationMapper locationMapper;
    private final CountMapper countMapper;
    private final LikeMapper likeMapper;

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

        // 최종 경로
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

    public List<PostResponseDto> getSortedPosts(String sort, String users_id) {
        // 1. 게시물 목록 가져오기 (정렬은 DB 쿼리에서)
        List<Post> posts = sort.equals("popular")
                ? postMapper.findPopularPosts()
                : postMapper.findLatestPosts();

        // 2. 게시물 ID 목록 추천
        List<Integer> postList = posts.stream()
                .map(Post::getId)
                .toList();

        // 3. 좋아요한 게시물 ID 목록 가져오기
        Set<Integer> likedPostSet = (users_id != null)
                ? new HashSet<>(likeMapper.findLikedPostList(postList, users_id))
                : new HashSet<>();

        // 4. DTO 변환 (liked 여부 포함)
        return posts.stream()
                .map(post -> PostResponseDto.from(post, likedPostSet.contains(post.getId())))
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

//    public void increaseViewCount(int id) {
//        postMapper.updateViewCount(id);
//    }

    public PostResponseDto getPostById(int id, String users_id) {
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

            boolean liked = (users_id != null && likeMapper.isLiked(id, users_id) != null); // ✅ 좋아요 여부 체크

            return PostResponseDto.from(post, locations, comments, counts, liked); // ✅ liked 값 포함
    }

}
