package com.trip_gg.mapper;

import com.trip_gg.domain.District;
import com.trip_gg.dto.DistrictResponseDto;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface DistrictMapper {
    List<DistrictResponseDto> findAllByCity(int cities_id);
}
