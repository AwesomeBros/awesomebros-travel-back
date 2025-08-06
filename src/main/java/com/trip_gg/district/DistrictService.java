package com.trip_gg.district;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DistrictService {

    private final DistrictMapper districtMapper;

    public List<DistrictResponseDto> getAllDistricts(int cities_id){
        List<DistrictResponseDto> districts = districtMapper.findAllByCity(cities_id);
        return districts;
    }
}
