package com.trip_gg.count;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/counts")
@RequiredArgsConstructor
public class CountController {

    private final CountService countService;

    @GetMapping("/{posts_id}")
    public ResponseEntity<CountResponseDto> getCounts(@PathVariable int posts_id) {
        CountResponseDto responseDto = countService.getCountByPostId(posts_id);
//        System.out.println("===== 컨트롤러 카운트 데이터 : " + responseDto);
        return ResponseEntity.ok(responseDto);
    }
}
