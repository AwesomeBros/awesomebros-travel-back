package com.trip_gg.mapper;

import com.trip_gg.domain.Location;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface LocationMapper {
    void insertLocation(Location location);

    List<Location> findAll();

    List<Location> getLocationByPostId(int posts_id);

    void deleteLocationByPostId(int id);

    void updateLocation(Location location);
}
