package com.trip_gg.district;

import com.trip_gg.district.DistrictResponseDto;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface DistrictMapper {
    List<DistrictResponseDto> findAllByCity(int cities_id);
}
