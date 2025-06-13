package com.trip_gg.dto;

import com.trip_gg.domain.Post;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class PostResponseDto {

    private Long id;
    private String title;
    private String content;
    private String users_id;
    private String cities_id;
    private String slug;
    private String imageUrl;
    private int viewCount;
    private String url;
    private LocalDateTime createdAt;

    public static PostResponseDto from(Post post) {
        return PostResponseDto.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .users_id(post.getUsers_id())
                .cities_id(post.getCities_id())
                .slug(post.getSlug())
                .imageUrl(post.getImageUrl())
                .viewCount(post.getViewCount())
                .url(post.getUrl())
                .createdAt(post.getCreatedAt())
                .build();
    }
}