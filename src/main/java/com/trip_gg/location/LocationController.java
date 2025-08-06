package com.trip_gg.location;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController // ✅ @Controller → @RestController로 변경 (JSON 반환 목적)
@RequestMapping("/api/locations")
@RequiredArgsConstructor
public class LocationController {

    private final LocationService locationService;

    // ✅ Location 저장
    @PostMapping
    public ResponseEntity<Void> save(@RequestBody Location location) {
        locationService.saveLocation(location);
//        System.out.println("받아온 데이터: " + location);
        return ResponseEntity.ok().build();
    }

    // ✅ 모든 Location 조회
    @GetMapping
    public ResponseEntity<List<Location>> findAll() {
        return ResponseEntity.ok(locationService.getAllLocations());
    }

    // ✅ 특정 posts_id로 Location 목록 조회
    @GetMapping("/{posts_id}")
    public ResponseEntity<List<Location>> findById(@PathVariable("posts_id") int posts_id) {
        List<Location> locations = locationService.getLocationById(posts_id);
        return ResponseEntity.ok(locations);
    }

}
