package com.trip_gg.service;

import com.trip_gg.domain.City;
import com.trip_gg.dto.CityResponseDto;
import com.trip_gg.dto.CountryResponseDto;
import com.trip_gg.mapper.CityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CityService {

    private final CityMapper cityMapper;

    public List<CityResponseDto> getAllCities(int countryId){
        List<CityResponseDto> cities = cityMapper.findAllByCountry(countryId);
        return cities;
    }
}
