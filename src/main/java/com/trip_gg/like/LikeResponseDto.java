package com.trip_gg.like;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LikeResponseDto {
    private int posts_id;
    private String users_id;
}
