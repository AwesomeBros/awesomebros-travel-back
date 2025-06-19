package com.trip_gg.controller;

import com.trip_gg.domain.City;
import com.trip_gg.dto.CityResponseDto;
import com.trip_gg.dto.CountryResponseDto;
import com.trip_gg.service.CityService;
import com.trip_gg.service.CountryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/cities")
@RequiredArgsConstructor
public class CityController {

    private final CityService cityService;

    @GetMapping
    public ResponseEntity<List<CityResponseDto>> getAllCities(@RequestParam("countries_id") int countries_id){
        List<CityResponseDto> cities = cityService.getAllCities(countries_id);
        return ResponseEntity.ok(cities);
    }
}
