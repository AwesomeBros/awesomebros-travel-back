package com.travel.demo.dto;

import com.travel.demo.domain.Post;
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
    private int viewCount;
    private LocalDateTime createdAt;

    public static PostResponseDto from(Post post) {
        return PostResponseDto.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .country(post.getCountry())
                .region(post.getRegion())
                .viewCount(post.getViewCount())
                .createdAt(post.getCreatedAt())
                .build();
    }
}