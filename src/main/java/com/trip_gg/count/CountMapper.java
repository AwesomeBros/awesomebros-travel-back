package com.trip_gg.count;

import com.trip_gg.count.Count;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CountMapper {
    Count getCountByPostId(int posts_id);
}
