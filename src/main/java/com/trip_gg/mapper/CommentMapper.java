package com.trip_gg.mapper;

import com.trip_gg.domain.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CommentMapper {

    //  댓글 저장
    void insertComment(Comment comment);

    //  특정 게시글의 댓글 목록 (페이징)
    List<Comment> findCommentsByPostId(
            @Param("posts_id") int posts_id,
            @Param("offset") int offset,
            @Param("limit") int limit
    );

    // 댓글 수 (페이지네이션용)
    int countCommentsByPostId(int posts_id);
}
