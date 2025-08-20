package com.trip_gg.post;

import com.trip_gg.post.Post;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface PostMapper {
    void insertPost(Post post);
    List<Post> findLatestPosts();
    List<Post> findPopularPosts();
    List<Post> getPostsByCity(String city);
    List<Post> getAllPosts();
    Post getPostById(int id);
    void update(Post post);
    void upsertCounts(@Param("posts_id") int posts_id);

    int checkLocationValidity(@Param("countries_id") Long countries_id,
                              @Param("cities_id") Long cities_id,
                              @Param("districts_id") Long districts_id);

    List<Post> findPostsByLocation(@Param("country") String country,
                                   @Param("city") String city,
                                   @Param("district") String district);
}
