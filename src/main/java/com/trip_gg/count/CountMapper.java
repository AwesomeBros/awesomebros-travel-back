package com.trip_gg.count;

import com.trip_gg.count.Count;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface CountMapper {
    Count getCountByPostId(int posts_id);

    void upsertOnView(@Param("posts_id") int posts_id);
}
