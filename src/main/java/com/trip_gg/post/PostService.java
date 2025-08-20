package com.trip_gg.post;

import com.trip_gg.comment.Comment;
import com.trip_gg.common.Pagination;
import com.trip_gg.count.Count;
import com.trip_gg.like.LikeMapper;
import com.trip_gg.location.Location;
import com.trip_gg.comment.CommentResponseDto;
import com.trip_gg.count.CountResponseDto;
import com.trip_gg.location.LocationDto;
import com.trip_gg.count.CountMapper;
import com.trip_gg.location.LocationMapper;
import jakarta.servlet.http.HttpServletRequest;
// import jakarta.servlet.http.HttpServletResponse; // ❌ 미사용 임포트 제거  // ✅ 변경
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
// import lombok.Value; // ❌ 잘못된 임포트 (롬복 Value 아님)          // ✅ 변경
import org.springframework.beans.factory.annotation.Value;                 // ✅ 변경
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;                                                   // ✅ 변경 (Files.move 사용)
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

    @Value("${file.upload.root}")
    private String uploadRoot;               // e.g. /home/gyubuntu/project/media/trip_gg_uploads

    @Value("${file.upload.public-path:/uploads}")
    private String publicPathPrefix;         // e.g. /uploads

    @Transactional
    public void createPost(PostRequestDto postRequestDto, HttpServletRequest request)
            throws IllegalAccessException, IOException {
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
        String relativeUrl = null;

        if (originUrl != null && originUrl.contains("/temp/")) {
            relativeUrl = moveFileFromTempToImages(originUrl, post.getId());
            String baseUrl = buildBaseUrl(request);
            post.setUrl(baseUrl + relativeUrl);
        }

        postMapper.update(post);

        List<Location> locations = postRequestDto.toLocation(post.getId());
        for (Location loc : locations) {
            loc.setPosts_id(post.getId());
            locationMapper.insertLocation(loc);
        }
    }

    @Transactional
    public void update(int id, PostRequestDto postRequestDto, HttpServletRequest request)
            throws IOException, IllegalAccessException {
        Post post = postRequestDto.toPost();
        post.setId(id);

        int isValid = postMapper.checkLocationValidity(
                post.getCountries_id(), post.getCities_id(), post.getDistricts_id());
        if (isValid == 0) {
            throw new IllegalAccessException("국가, 도시, 지역 선택이 잘못되었습니다.");
        }

        String originUrl = postRequestDto.getUrl();
        String relativeUrl = null;

        if (originUrl != null && originUrl.contains("/temp/")) {
            relativeUrl = moveFileFromTempToImages(originUrl, id);
            String baseUrl = buildBaseUrl(request);
            post.setUrl(baseUrl + relativeUrl);
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

    /**
     * temp 경로의 파일을 서버 저장소(images/{postId}/)로 이동시키고,
     * 브라우저에서 접근 가능한 상대경로(/uploads/images/{postId}/{fileName})를 반환합니다.
     */
    @Transactional
    private String moveFileFromTempToImages(String tempUrl, int posts_id) throws IOException {
        if (tempUrl == null) throw new IOException("파일 경로가 null입니다.");

        String fileName = tempUrl.substring(tempUrl.lastIndexOf("/") + 1);

        // 실제 파일시스템 경로
        Path tempPath = Paths.get(uploadRoot, "temp", fileName);                 // ✅ 변경
        Path destDir = Paths.get(uploadRoot, "images", String.valueOf(posts_id));// ✅ 변경
        Path destPath = destDir.resolve(fileName);                                // ✅ 변경

        if (!Files.exists(tempPath)) {
            throw new IOException("임시 파일이 존재하지 않습니다: " + tempPath);
        }
        if (!Files.exists(destDir)) {
            Files.createDirectories(destDir);                                     // ✅ 변경
        }

        // renameTo 대신 Files.move 사용 (다른 파일시스템/권한에서도 안정적)   // ✅ 변경
        try {
            Files.move(tempPath, destPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            throw new IOException("파일 이동 실패: " + tempPath + " → " + destPath, ex);
        }

        return publicPathPrefix + "/images/" + posts_id + "/" + fileName;
    }

    // 요청으로부터 베이스 URL을 생성 (scheme://host[:port])
    private String buildBaseUrl(HttpServletRequest request) {
        return ServletUriComponentsBuilder.fromRequestUri(request)
                .replacePath(null)
                .build()
                .toUriString();
    }

    // 조회수 증가
    @Transactional
    public void increaseViewCount(int postId) {
        countMapper.upsertOnView(postId);
    }

    public List<PostResponseDto> getSortedPosts(String sort, String users_id) {
        List<Post> posts = sort.equals("popular")
                ? postMapper.findPopularPosts()
                : postMapper.findLatestPosts();

        List<Integer> postList = posts.stream().map(Post::getId).toList();

        Set<Integer> likedPostSet = (users_id != null)
                ? new HashSet<>(likeMapper.findLikedPostList(postList, users_id))
                : new HashSet<>();

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
                ? List.of(CountResponseDto.from(count))
                : List.of();

        boolean liked = (users_id != null && likeMapper.isLiked(id, users_id) != null);
        return PostResponseDto.from(post, locations, comments, counts, liked);
    }

//    public List<PostResponseDto> searchPosts(Integer countries_id,
//                                             Integer cities_id,
//                                             Integer districts_id) {
//        List<Post> posts = postMapper.findPostsByLocation(countries_id, cities_id, districts_id);
//
//        return posts.stream()
//                .map(PostResponseDto::from)
//                .collect(Collectors.toList());
//    }

    // ✅ 국가/도시/지역 이름 기반 검색 (페이지네이션은 자바단 공통 메서드 사용 가정)
    public List<PostResponseDto> searchByNames(String country,
                                               String city,
                                               String district) {
        return postMapper.findPostsByLocation(country, city, district)
                .stream()
                .map(PostResponseDto::from) // ✅ users_id/liked/view_count 제외 버전 사용
                .toList();
    }

//    // 도시별 후기 조회 (페이지네이션 적용)
//    public Pagination<PostResponseDto> getPostsByCityPaged(String city, int page, int size) {
//        List<PostResponseDto> all = postMapper.getPostsByCity(city).stream()
//                .map(PostResponseDto::from)
//                .toList();
//        return paginate(all, page, size);
//    }

    /**
     * 공통 페이지네이션 메서드
     *
     * @param all  전체 데이터 리스트
     * @param page 요청 페이지 (1부터 시작)
     * @param size 페이지 크기
     */
    <T> Pagination<T> paginate(List<T> all, int page, int size) {
        int total = all.size();
        int fromIndex = Math.min((page - 1) * size, total);
        int toIndex = Math.min(fromIndex + size, total);

        List<T> content = all.subList(fromIndex, toIndex);

        return Pagination.<T>builder()
                .content(content)
                .page(page)
                .size(size)
                .totalElements(total)
                .totalPages((int) Math.ceil((double) total / size))
                .build();
    }
}
