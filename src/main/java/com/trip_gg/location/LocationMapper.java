package com.trip_gg.location;

import com.trip_gg.location.Location;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface LocationMapper {

    // ✅ Location 저장
    void insertLocation(Location location);

    // ✅ 전체 Location 조회
    List<Location> findAll();

    // ✅ 특정 게시글(post)에 연결된 Location 조회
    List<Location> getLocationById(int posts_id);

    // ✅ 특정 게시글의 Location 삭제
    void deleteLocationByPostId(int posts_id);

    // ✅ Location 수정
    void updateLocation(Location location);
}
