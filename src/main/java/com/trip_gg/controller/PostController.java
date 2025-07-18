package com.trip_gg.controller;

import com.trip_gg.domain.Post;
import com.trip_gg.dto.PostRequestDto;
import com.trip_gg.dto.PostResponseDto;
import com.trip_gg.jwt.JwtTokenProvider;
import com.trip_gg.service.PostService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final JwtTokenProvider jwtTokenProvider;

    /* 생성 파트 */
    // 글 작성 - JSON만 처리
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> createPost(@RequestBody PostRequestDto postRequestDto,
                                             HttpServletRequest request){
        try{
            String token = jwtTokenProvider.resolveToken(request);
            String users_id = jwtTokenProvider.getUserIdFromToken(token);
            postRequestDto.setUsers_id(users_id);

            postService.createPost(postRequestDto);
//            System.out.println("=====토큰 정보1 : " + token + "=====");
//            System.out.println("=====토큰 정보2 : " + postService + "=====");
            return ResponseEntity.ok("글 작성 완료");
        } catch (IllegalAccessException exception) {
            exception.printStackTrace();
            return ResponseEntity.internalServerError().body("업로드 중 오류 발생: " + exception.getMessage());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    /* 조회 파트 */
    // 최신순 또는 인기순 게시글 목록 조회 (쿼리 파라미터: ?sort=latest | popular)
    @GetMapping
    public List<PostResponseDto> getPostsSorted(@RequestParam("sort") String sort) {

        return postService.getSortedPosts(sort);
    }

    // 지역 기준 게시글 목록 조회 (쿼리 파라미터: ?region=서울 등)
    @GetMapping("/cities")
    public List<PostResponseDto> getPostsByCity(@RequestParam("city") String city) {
        return postService.getPostsByCity(city);
    }

    // 전체 게시글 목록 조회
    @GetMapping("/all")
    public List<Post> getPostList(Model model) {
        List<Post> posts = postService.getAllPosts();
        model.addAttribute("posts", posts);
        return posts;
    }

    // 단일 게시글 상세 조회
    @GetMapping("/{id}")
    public ResponseEntity<PostResponseDto> getPostById(@PathVariable int id) {
        PostResponseDto post = postService.getPostById(id);
//        System.out.println("=====CONTROLLER POST에 들어있는 데이터 : " + post.getPosts_id() + "=====");
        return ResponseEntity.ok(post);
    }

    // 게시글 수정
    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> update(@RequestBody PostRequestDto postRequestDto,
                                         @PathVariable int id,
                                         HttpServletRequest request) {
        try {
            String token = jwtTokenProvider.resolveToken(request);
            String users_id = jwtTokenProvider.getUserIdFromToken(token);
            postRequestDto.setUsers_id(users_id);

            postService.update(id, postRequestDto);
            return ResponseEntity.ok("수정 완료");
        } catch (IOException | IllegalAccessException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("수정 중 오류 발생: " + e.getMessage());
        }
    }
}
