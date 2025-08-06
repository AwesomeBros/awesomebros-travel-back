package com.trip_gg.country;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.trip_gg.country.CountryResponseDto;

@Mapper
public interface CountryMapper {

    List<CountryResponseDto> findAll();

}
