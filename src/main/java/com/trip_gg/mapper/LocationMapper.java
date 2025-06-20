package com.trip_gg.mapper;

import com.trip_gg.domain.Location;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface LocationMapper {
    void insertLocation(Location location);

    List<Location> findAll();
}
