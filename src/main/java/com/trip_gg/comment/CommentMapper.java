package com.trip_gg.comment;

import com.trip_gg.comment.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CommentMapper {

    //  댓글 저장
    void insertComment(Comment comment);

    //  특정 게시글의 댓글 목록 (페이징)
    List<Comment> findCommentsByPostId(
            @Param("posts_id") int posts_id
    );

    // 댓글 수 (페이지네이션용)
    int countCommentsByPostId(int posts_id);

    // counts 테이블의 comment_count 증가
    void increaseCommentCount(@Param("posts_id") int posts_id);

    List<Comment> findCommentsByUserId(@Param("users_id") String users_id);

    int updateMyComment(@Param("id") int id,
                        @Param("users_id") String usersId,
                        @Param("content") String content);

    int deleteMyComment(@Param("id") int comments_id,
                        @Param("users_id") String users_id);
}
