package com.trip_gg.country;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/countries")
@RequiredArgsConstructor
public class CountryController {
    private final CountryService countryService;

    @GetMapping
    public ResponseEntity<List<CountryResponseDto>> getAllCountries() {
        List<CountryResponseDto> countries = countryService.getAllCountries();
        return ResponseEntity.ok(countries);
    }
}
