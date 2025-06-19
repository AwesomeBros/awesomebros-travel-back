package com.trip_gg.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.trip_gg.dto.CountryResponseDto;

@Mapper
public interface CountryMapper {

    List<CountryResponseDto> findAll();

}
