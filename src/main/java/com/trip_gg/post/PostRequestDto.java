package com.trip_gg.post;

import com.trip_gg.location.Location;
import com.trip_gg.location.LocationDto;
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

    // 지역 카테고리
    private Long countries_id;
    private Long cities_id;
    private Long districts_id;

    private List<LocationDto> locations;

    private String url;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setUsers_id(String users_id) { this.users_id = users_id;}

    // Post 엔티티로 변환
    public Post toPost() {

        Post post = new Post();
        post.setTitle(this.title);
        post.setContent(this.content);
        post.setUsers_id(this.users_id);
        post.setSlug(this.slug);
        post.setUrl(this.url);
        post.setCountries_id(this.countries_id);
        post.setCities_id(this.cities_id);
        post.setDistricts_id(this.districts_id);
//        post.setPost_id(this.post_id);
        post.setCreated_at(LocalDateTime.now());

        System.out.println("[DEBUG] toPost 변환 결과 : " + post);

        return post;
    }

    // Location 엔티티로 변환
    public List<Location> toLocation(int posts_id) {
            return this.locations.stream()
                    .map(coord -> Location.builder()
                            .name(coord.getName())
                            .lat(coord.getLat())
                            .lng(coord.getLng())
                            .posts_id(posts_id)
                        .created_at(LocalDateTime.now())
                        .build())
                .collect(Collectors.toList());
    }
}
