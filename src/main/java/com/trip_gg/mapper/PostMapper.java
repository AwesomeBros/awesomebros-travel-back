package com.trip_gg.mapper;

import com.trip_gg.domain.Post;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface PostMapper {
    void insertPost(Post post);
    List<Post> findLatestPosts();
    List<Post> findPopularPosts();
    List<Post> findByRegion(String region);
    List<Post> getAllPosts();
    Post getPostById(Long id);
}
