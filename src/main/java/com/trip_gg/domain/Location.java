package com.trip_gg.domain;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class Location {
    private int id;
    private String name;
    private double lat;
    private double lng;
    private LocalDateTime created_at;
    private int posts_id;

    @Override
    public String toString() {
        return "Location{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", lat=" + lat +
                ", lng=" + lng +
                ", created_at=" + created_at +
                ", posts_id=" + posts_id +
                '}';
    }
}
