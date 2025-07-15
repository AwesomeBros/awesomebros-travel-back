package com.trip_gg.service;

import com.trip_gg.domain.Location;
import com.trip_gg.mapper.LocationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LocationService {

    private final LocationMapper locationMapper;

    // ✅ Location 저장
    public void saveLocation(Location location) {
        locationMapper.insertLocation(location);
    }

    // ✅ 전체 Location 조회
    public List<Location> getAllLocations() {
        return locationMapper.findAll();
    }

    // ✅ 특정 게시글(post_id) 기준 Location 목록 조회
    public List<Location> getLocationById(int posts_id) {
        return locationMapper.getLocationById(posts_id);
    }
}
