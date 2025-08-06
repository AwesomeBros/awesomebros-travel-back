package com.trip_gg.country;

import java.util.List;

import org.springframework.stereotype.Service;

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
