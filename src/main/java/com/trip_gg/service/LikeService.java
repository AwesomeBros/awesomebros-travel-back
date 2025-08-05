package com.trip_gg.service;

import com.trip_gg.mapper.LikeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LikeService {

    private final LikeMapper likeMapper;

    public void toggleLike(int posts_id, String users_id) {
        // ✅ 이미 좋아요 했는지 확인
        Integer existing = likeMapper.checkLiked(posts_id, users_id);

        if (existing == null) {
            // ✅ 없으면 추가
            likeMapper.insertLike(posts_id, users_id);
            likeMapper.increaseLikeCount(posts_id);
        } else {
            // ✅ 있으면 삭제
            likeMapper.deleteLike(posts_id, users_id);
            likeMapper.decreaseLikeCount(posts_id);
        }
    }

    public int getLike_count(int posts_id) {
        return likeMapper.selectLike_count(posts_id);
    }

    // 좋아요 여부 반환
    public boolean isLiked(int posts_id, String users_id) {
        return likeMapper.existsLike(posts_id, users_id);
    }
}
