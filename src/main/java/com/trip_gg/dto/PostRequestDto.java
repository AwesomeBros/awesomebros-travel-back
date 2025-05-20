package com.trip_gg.dto;

import com.trip_gg.domain.Post;
import lombok.Getter;

@Getter
public class PostRequestDto {
    private String title;
    private String content;
    private String country;
    private String region;
    private String imageUrl;

    public Post toPost() {
        Post post = new Post();
        post.setTitle(this.title);
        post.setContent(this.content);
        post.setCountry(this.country);
        post.setRegion(this.region);
        post.setImageUrl(this.imageUrl);
        post.setViewCount(0);
        return post;
    }
}
