package com.trip_gg.mapper;

import com.trip_gg.domain.City;
import com.trip_gg.dto.CityResponseDto;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CityMapper {
    List<CityResponseDto> findAllByCountry(int countryId);
}
