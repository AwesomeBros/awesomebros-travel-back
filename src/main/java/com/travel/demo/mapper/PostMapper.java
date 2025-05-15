package com.travel.demo.mapper;

import com.travel.demo.domain.Post;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface PostMapper {
    List<Post> findLatestPosts();
    List<Post> findPopularPosts();
    List<Post> findByRegion(String region);
}
