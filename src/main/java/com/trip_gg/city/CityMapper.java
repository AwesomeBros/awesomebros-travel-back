package com.trip_gg.city;

import com.trip_gg.city.CityResponseDto;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CityMapper {
    List<CityResponseDto> findAllByCountry(int countries_id);
}
