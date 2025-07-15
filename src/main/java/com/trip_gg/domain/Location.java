package com.trip_gg.domain;

import lombok.*;

import java.time.LocalDateTime;

// ✅ MyBatis 매핑을 위한 단순 POJO 클래스
@Data // @Getter, @Setter, @ToString, @EqualsAndHashCode 포함
@NoArgsConstructor // 기본 생성자
@AllArgsConstructor // 전체 필드 생성자
@Builder
public class Location {

    private int id;                   // 위치 ID
    private String name;             // 장소 이름
    private double lat;              // 위도
    private double lng;              // 경도
    private LocalDateTime created_at; // 생성일시
    private int posts_id;            // 연관된 게시글 ID
}
