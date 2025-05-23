package com.trip_gg.dto;

import com.trip_gg.domain.Post;
import lombok.Getter;

@Getter
public class PostRequestDto {
    private String title;
    private String content;
    private String users_id;
    private Long cities_id;
    private String slug;
//    private String country;
//    private String region;
    private String imageUrl;
    private int viewCount;

    public Post toPost() {
        Post post = new Post();
        post.setTitle(this.title);
        post.setContent(this.content);
        post.setUsers_id(this.users_id);
        post.setCities_id(this.cities_id);
        post.setSlug(this.slug);
//        post.setCountry(this.country);
//        post.setRegion(this.region);
        post.setImageUrl(this.imageUrl);
        post.setViewCount(this.viewCount);
        return post;
    }
}
