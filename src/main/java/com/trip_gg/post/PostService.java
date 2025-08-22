package com.trip_gg.post;

import com.trip_gg.comment.Comment;
import com.trip_gg.comment.CommentMapper;
import com.trip_gg.comment.CommentRequestDto;
import com.trip_gg.comment.CommentResponseDto;
import com.trip_gg.common.Pagination;
import com.trip_gg.count.Count;
import com.trip_gg.count.CountMapper;
import com.trip_gg.count.CountResponseDto;
import com.trip_gg.like.LikeMapper;
import com.trip_gg.location.Location;
import com.trip_gg.location.LocationDto;
import com.trip_gg.location.LocationMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostMapper postMapper;
    private final LocationMapper locationMapper;
    private final CountMapper countMapper;
    private final LikeMapper likeMapper;
    private final CommentMapper commentMapper;

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
     *  내가 작성한 게시글 목록 조회
     */
    public Pagination<PostResponseDto> getPostsByUserId(String users_id, int page, int size) {  // ✅ 변경
        // 1) 전체 조회 (DB 레벨 페이징 필요하면 Mapper에 LIMIT/OFFSET 쿼리 추가로 교체 가능)
        List<Post> posts = postMapper.findPostsByUserId(users_id);

        // 2) DTO 변환
        List<PostResponseDto> allDtos = posts.stream()
                .map(p -> PostResponseDto.builder()
                        .id(p.getId())
                        .title(p.getTitle())
                        .content(p.getContent())
                        .users_id(p.getUsers_id())
                        .created_at(p.getCreated_at())
                        .url(p.getUrl())
                        .build())
                .toList();

        // 3) 자바단 페이지네이션 (공용 메서드 사용)
        return paginate(allDtos, page, size);
    }

    /** 내가 쓴 게시글 수정(소유자 검증 포함) */
    @Transactional
    public void updatePostByOwner(String users_id, int posts_id, PostRequestDto dto) throws IllegalAccessException, IOException {
        Post existing = postMapper.getPostById(posts_id);
        if (existing == null) throw new IllegalAccessException("게시글이 존재하지 않습니다.");
        if (!Objects.equals(existing.getUsers_id(), users_id)) {
            throw new IllegalAccessException("본인 게시글만 수정할 수 있습니다.");
        }

        Post toUpdate = dto.toPost();
        toUpdate.setId(posts_id);
        toUpdate.setUsers_id(users_id);

        int isValid = postMapper.checkLocationValidity(
                toUpdate.getCountries_id(), toUpdate.getCities_id(), toUpdate.getDistricts_id());
        if (isValid == 0) throw new IllegalAccessException("국가/도시/지역 선택이 잘못되었습니다.");

        // 이미지가 /temp 에 있으면 최종 경로로 이동 (요청객체가 없으니 절대URL 저장은 생략/유지)
        String originUrl = dto.getUrl();
        if (originUrl != null && originUrl.contains("/temp/")) {
            String relativeUrl = moveFileFromTempToImages(originUrl, posts_id);
            // 기존 URL 형식 유지: 상대경로만 저장하거나, 기존 absolute 형식이면 그대로 두세요.
            toUpdate.setUrl(relativeUrl);
        } else {
            toUpdate.setUrl(originUrl);
        }

        postMapper.update(toUpdate);

        // 위치 갱신
        locationMapper.deleteLocationByPostId(posts_id);
        for (Location loc : dto.toLocation(posts_id)) {
            loc.setPosts_id(posts_id);
            locationMapper.insertLocation(loc);
        }

        postMapper.upsertCounts(posts_id);
    }

    /** 내가 쓴 게시글 삭제(소유자 검증 포함) *//*
    @Transactional
    public void deletePostByOwner(String users_id, int posts_id) throws IllegalAccessException {
        Post existing = postMapper.getPostById(posts_id);
        if (existing == null) return;
        if (!Objects.equals(existing.getUsers_id(), users_id)) {
            throw new IllegalAccessException("본인 게시글만 삭제할 수 있습니다.");
        }

        // 연관 데이터 정리 (댓글/좋아요/위치/카운트)
        commentMapper.deleteByPostId(posts_id);     // ✅ Mapper 필요
        likeMapper.deleteAllByPostId(posts_id);     // ✅ Mapper 필요
        locationMapper.deleteLocationByPostId(posts_id);
        countMapper.deleteByPostId(posts_id);       // ✅ Mapper 필요

        postMapper.deleteById(posts_id);            // ✅ Mapper 필요
        // (원하면 업로드 이미지 파일 삭제도 추가 가능)
    }*/


    /** 내가 좋아요한 게시글 목록 */
    public Pagination<PostResponseDto> getLikedPostsByUserId(String users_id, int page, int size) {
        List<Post> posts = postMapper.findLikedPostsByUserId(users_id);
        List<PostResponseDto> allDtos = posts.stream().map(PostResponseDto::from).toList();
//        return posts.stream().map(PostResponseDto::from).toList();
        return paginate(allDtos, page, size);
    }


    /** 내가 단 댓글 목록  */
    // 조회(최신순)
    public Pagination<CommentResponseDto> getMyComments(String users_id, int page, int size) {
        List<Comment> comments = commentMapper.findCommentsByUserId(users_id);
        List<CommentResponseDto> allDtos = comments.stream().map(CommentResponseDto::from).toList();
        return paginate(allDtos, page, size);
    }

    // 수정
    public void updateCommentContentOnly(int commentsId, String content) {
        commentMapper.updateCommentById(commentsId, content);
    }



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
