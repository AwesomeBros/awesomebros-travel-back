package com.trip_gg.service;

import com.trip_gg.domain.Location;
import com.trip_gg.mapper.LocationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LocationService {
    private final LocationMapper locationMapper;

    public void saveLocation(Location location) {
        locationMapper.insertLocation(location);
    }

    public List<Location> getAllLocations() {
        return locationMapper.findAll();
    }

}
