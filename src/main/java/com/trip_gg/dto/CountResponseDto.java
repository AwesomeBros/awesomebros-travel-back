package com.trip_gg.dto;

import com.trip_gg.domain.Count;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.AccessLevel;

@Getter
@Builder
@AllArgsConstructor
public class CountResponseDto {

    private int like_count;
    private int comment_count;
    private int view_count;

    public static CountResponseDto from(Count count) {
        return CountResponseDto.builder()
                .like_count(count.getLike_count())
                .comment_count(count.getComment_count())
                .view_count(count.getView_count())
                .build();
    }
}
