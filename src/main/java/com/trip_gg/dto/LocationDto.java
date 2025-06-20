package com.trip_gg.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class LocationDto {
    private String name;
    private double lat;
    private double lng;
    private LocalDateTime created_at;
    private int post_id;
}
