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

    private int id;
    private String title;
    private String content;
    private boolean liked;
    private String users_id;
    private int posts_id;

    private String slug;
//    private String fileUrl;
    private String url;
    private LocalDateTime created_at;

    // 지역 카테고리
    private Long countries_id;
    private Long cities_id;
    private Long districts_id;

    private List<LocationDto> locations;
    private List<CommentResponseDto> comments;
    private List<CountResponseDto> counts;
    private List<LikeResponseDto> likes;

    // 기본 변환(좌표 없이)
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
                .build();
    }

    // 좌표 리스트 포함된 변환
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
                .build();
    }

    // 위치 , 댓글, 카운트 모두 포함 변환
    public static PostResponseDto from(Post post, List<LocationDto> locations, List<CommentResponseDto> comments, List<CountResponseDto> counts, boolean liked) {
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
                .build();
    }

    // 좋아요 여부만 포함하는 변환
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
                .build();
    }


    public int getPosts_id() {
        return posts_id;
    }
}