package com.trip_gg.dto;

import com.trip_gg.domain.Location;
import com.trip_gg.domain.Post;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class PostRequestDto {

    private String title;
    private String content;
    private String users_id;
    private String slug;
    private String url;
    private int viewCount;

    // 지역 카테고리
    private Long countries_id;
    private Long cities_id;
    private Long districts_id;

    // Location 정보
//    private String name;
//    private double lat;
//    private double lng;
//    private int post_id;
//    private LocalDateTime created_at;

    private List<LocationDto> locations;

    // Post 엔티티로 변환
    public Post toPost() {

        Post post = new Post();
        post.setTitle(this.title);
        post.setContent(this.content);
        post.setUsers_id(this.users_id);
        post.setSlug(this.slug);
        post.setUrl(this.url);
        post.setViewCount(this.viewCount);
        post.setCountries_id(this.countries_id);
        post.setCities_id(this.cities_id);
        post.setDistricts_id(this.districts_id);
//        post.setPost_id(this.post_id);
        post.setCreated_at(LocalDateTime.now());

        System.out.println("[DEBUG] toPost 변환 결과 : " + post);

        return post;
    }

    // Location 엔티티로 변환
    public List<Location> toLocation(int post_id) {
        return this.locations.stream()
                .map(coord -> Location.builder()
                        .name(coord.getName())
                        .lat(coord.getLat())
                        .lng(coord.getLng())
                        .post_id(post_id)
                        .created_at(LocalDateTime.now())
                        .build())
                .collect(Collectors.toList());
    }
}
