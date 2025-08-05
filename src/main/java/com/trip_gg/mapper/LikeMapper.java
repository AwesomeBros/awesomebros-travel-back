package com.trip_gg.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface LikeMapper {

    // ✅ 좋아요 여부 확인
    Integer checkLiked(@Param("posts_id") int posts_id, @Param("users_id") String users_id);

    // ✅ 좋아요 추가
    void insertLike(@Param("posts_id") int posts_id, @Param("users_id") String users_id);

    // ✅ 좋아요 삭제
    void deleteLike(@Param("posts_id") int posts_id, @Param("users_id") String users_id);

    // ✅ 카운트 증가
    void increaseLikeCount(@Param("posts_id") int posts_id);

    // ✅ 카운트 감소
    void decreaseLikeCount(@Param("posts_id") int posts_id);

    // ✅ 좋아요 수 조회
    int selectLike_count(@Param("posts_id") int posts_id);

    // 좋아요 표시
    boolean existsLike(@Param("posts_id") int posts_id, @Param("users_id") String users_id);
}
