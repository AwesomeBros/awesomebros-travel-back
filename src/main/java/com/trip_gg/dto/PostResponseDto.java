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
    private String country;
    private String region;
    private String imageUrl;
    private int viewCount;
    private LocalDateTime createdAt;

    public static PostResponseDto from(Post post) {
        return PostResponseDto.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .country(post.getCountry())
                .region(post.getRegion())
                .imageUrl(post.getImageUrl())
                .viewCount(post.getViewCount())
                .createdAt(post.getCreatedAt())
                .build();
    }
}