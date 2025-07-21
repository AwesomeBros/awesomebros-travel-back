package com.trip_gg.service;

import com.trip_gg.domain.Count;
import com.trip_gg.dto.CountResponseDto;
import com.trip_gg.mapper.CountMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CountService {

    private final CountMapper countMapper;

    public CountResponseDto getCountByPostId(int posts_id) {
        Count count = countMapper.getCountByPostId(posts_id);

        // ✅ count 값 확인 출력
        System.out.println("===== 서비스 카운트 데이터: " + count);

        if (count == null) {
            System.out.println("count가 null입니다. 기본값 반환.");
            return CountResponseDto.builder()
                    .view_count(0)
                    .comment_count(0)
                    .like_count(0)
                    .build();
        }

        // ✅ 각 필드 값도 개별 확인
        System.out.println("like_count = " + count.getLike_count());
        System.out.println("view_count = " + count.getView_count());
        System.out.println("comment_count = " + count.getComment_count());

        return CountResponseDto.from(count);
    }
}
