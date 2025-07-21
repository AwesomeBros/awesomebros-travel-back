package com.trip_gg.mapper;

import com.trip_gg.domain.Count;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CountMapper {
    Count getCountByPostId(int posts_id);
}
