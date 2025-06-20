package com.trip_gg.controller;

import com.trip_gg.domain.District;
import com.trip_gg.dto.DistrictResponseDto;
import com.trip_gg.service.DistrictService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/districts")
@RequiredArgsConstructor
public class DistrictController {

    private final DistrictService districtService;

    @GetMapping
    public ResponseEntity<List<DistrictResponseDto>> getAllDistricts(@RequestParam("cities_id") int cities_id){
        List<DistrictResponseDto> districts = districtService.getAllDistricts(cities_id);
        return ResponseEntity.ok(districts);
    }
}
