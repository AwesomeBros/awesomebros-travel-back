package com.trip_gg.post;

import com.trip_gg.comment.CommentResponseDto;
import com.trip_gg.count.CountResponseDto;
import com.trip_gg.like.LikeResponseDto;
import com.trip_gg.location.LocationDto;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class PostResponseDto {

    // ── 기본 필드 ───────────────────────────────────────────────────────────────
    private int id;
    private String title;
    private String content;
    private boolean liked;

    private String users_id;                // 작성자 ID (UUID)
    private int posts_id;                   // 필요 시 사용되는 게시글 ID
    private String post_writer_nickname;    // ✅ 게시글 작성자 닉네임

    private String slug;
    private String url;
    private LocalDateTime created_at;

    // 지역 카테고리
    private Long countries_id;
    private Long cities_id;
    private Long districts_id;

    // 연관 데이터
    private List<LocationDto> locations;
    private List<CommentResponseDto> comments;
    private List<CountResponseDto> counts;
    private List<LikeResponseDto> likes;

    // ── 변환 메서드: 좌표 없이 ────────────────────────────────────────────────
    public static PostResponseDto from(Post post) {
        return PostResponseDto.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .users_id(post.getUsers_id())
                .countries_id(post.getCountries_id())
                .cities_id(post.getCities_id())
                .districts_id(post.getDistricts_id())
                .slug(post.getSlug())
                .url(post.getUrl())
                .created_at(post.getCreated_at())
                .post_writer_nickname(post.getPost_writer_nickname()) // ★ 추가
                .build();
    }

    // ── 변환 메서드: 좌표 포함 ────────────────────────────────────────────────
    public static PostResponseDto from(Post post, List<LocationDto> locations) {
        return PostResponseDto.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .users_id(post.getUsers_id())
                .countries_id(post.getCountries_id())
                .cities_id(post.getCities_id())
                .districts_id(post.getDistricts_id())
                .slug(post.getSlug())
                .url(post.getUrl())
                .created_at(post.getCreated_at())
                .locations(locations)
                .post_writer_nickname(post.getPost_writer_nickname()) // ★ 추가
                .build();
    }

    // ── 변환 메서드: 위치, 댓글, 카운트, 좋아요 포함 ───────────────────────────
    public static PostResponseDto from(
            Post post,
            List<LocationDto> locations,
            List<CommentResponseDto> comments,
            List<CountResponseDto> counts,
            boolean liked
    ) {
        return PostResponseDto.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .users_id(post.getUsers_id())
                .countries_id(post.getCountries_id())
                .cities_id(post.getCities_id())
                .districts_id(post.getDistricts_id())
                .slug(post.getSlug())
                .url(post.getUrl())
                .created_at(post.getCreated_at())
                .locations(locations)
                .comments(comments)
                .counts(counts)
                .liked(liked)
                .post_writer_nickname(post.getPost_writer_nickname()) // ★ 추가
                .build();
    }

    // ── 변환 메서드: 좋아요 여부만 포함 ───────────────────────────────────────
    public static PostResponseDto from(Post post, boolean liked) {
        return PostResponseDto.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .users_id(post.getUsers_id())
                .countries_id(post.getCountries_id())
                .cities_id(post.getCities_id())
                .districts_id(post.getDistricts_id())
                .slug(post.getSlug())
                .url(post.getUrl())
                .created_at(post.getCreated_at())
                .liked(liked)
                .post_writer_nickname(post.getPost_writer_nickname()) // ★ 추가
                .build();
    }

    // 필요 시 사용 (기존 코드 유지)
    public int getPosts_id() {
        return posts_id;
    }
}