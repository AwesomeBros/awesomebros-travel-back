package com.trip_gg.city;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CityService {

    private final CityMapper cityMapper;

    public List<CityResponseDto> getAllCities(int countries_id){
        List<CityResponseDto> cities = cityMapper.findAllByCountry(countries_id);
        return cities;
    }
}
