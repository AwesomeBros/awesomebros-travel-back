package com.trip_gg.location;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class LocationDto {
    private String name;
    private double lat;
    private double lng;
    private LocalDateTime created_at;
    private int posts_id;

    // Entity -> Dto 변환 메서드
    public static LocationDto from(Location location) {
        return LocationDto.builder()
                .name(location.getName())
                .lat(location.getLat())
                .lng(location.getLng())
                .posts_id(location.getPosts_id())
                .created_at(location.getCreated_at())
                .build();
    }
}
