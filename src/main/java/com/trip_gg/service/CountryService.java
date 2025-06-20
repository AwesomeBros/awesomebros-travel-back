package com.trip_gg.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.trip_gg.dto.CountryResponseDto;
import com.trip_gg.mapper.CountryMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CountryService {
    private final CountryMapper countryMapper;

    public List<CountryResponseDto> getAllCountries() {
        List<CountryResponseDto> countries = countryMapper.findAll();
        return countries;
    }

}
