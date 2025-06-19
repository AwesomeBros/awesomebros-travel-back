package com.trip_gg.service;

import com.trip_gg.domain.District;
import com.trip_gg.dto.DistrictResponseDto;
import com.trip_gg.mapper.DistrictMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DistrictService {

    private final DistrictMapper districtMapper;

    public List<DistrictResponseDto> getAllDistricts(int cityId){
        List<DistrictResponseDto> districts = districtMapper.findAllByCity(cityId);
        return districts;
    }
}
