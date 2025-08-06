package com.trip_gg.post;

import com.trip_gg.post.Post;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

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

    // 위치 논리 일관성 검증용 메서드
   /* @Select("""
            SELECT COUNT(*)
            FROM districts d
            JOIN cities c ON d.cities_id = c.id
            JOIN countries co ON c.countries_id = co.id
            WHERE d.id = #{districts_id}
                AND c.id = #{cities_id}
                AND co.id = #{countries_id}
            """)*/
    /*int checkLocationValidity(@Param("countries_id") Long countriesId,
                              @Param("cities_id") Long citiesId,
                              @Param("districts_id") Long districtsId);*/

    @Select("SELECT COUNT(*) FROM districts d " +
            "JOIN cities c ON d.cities_id = c.id " +
            "JOIN countries co ON c.countries_id = co.id " +
            "WHERE co.id = #{countries_id} AND c.id = #{cities_id} AND d.id = #{districts_id}")
    int checkLocationValidity(@Param("countries_id") Long countries_id,
                              @Param("cities_id") Long cities_id,
                              @Param("districts_id") Long districts_id);

}
