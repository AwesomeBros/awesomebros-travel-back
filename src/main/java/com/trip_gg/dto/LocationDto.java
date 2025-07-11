package com.trip_gg.dto;

import com.trip_gg.domain.Location;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

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
                .build();
    }
}
