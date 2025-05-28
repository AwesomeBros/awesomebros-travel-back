package com.trip_gg.mapper;

import com.trip_gg.domain.Post;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface PostMapper {
    void insertPost(Post post);
    List<Post> findLatestPosts();
    List<Post> findPopularPosts();
    List<Post> findByRegion(String region);
    List<Post> getAllPosts();
    Post getPostById(Long id);

    // 위치 논리 일관성 검증용 메서드
    @Select("""
            SELECT COUNT(*)
            FROM districts d
            JOIN cities c ON d.cities_id = c.id
            JOIN countries co ON c.countries_id = co.id
            WHERE d.id = #{districts_id}
                AND c.id = #{cities_id}
                AND co.id = #{countries_id}
            """)
    int checkLocationValidity(@Param("countries_id") Long countriesId,
                              @Param("cities_id") Long citiesId,
                              @Param("districts_id") Long districtsId);
}
