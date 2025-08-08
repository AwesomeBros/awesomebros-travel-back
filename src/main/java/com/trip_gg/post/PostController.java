package com.trip_gg.post;

import com.trip_gg.post.Post;
import com.trip_gg.post.PostRequestDto;
import com.trip_gg.post.PostResponseDto;
import com.trip_gg.post.PostService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    /* ===========================
       📌 생성 파트
       =========================== */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> createPost(@RequestBody PostRequestDto postRequestDto,
                                             HttpServletRequest request) {
        try {
            // ✅ SecurityContext에서 인증 정보(username = users_id) 추출
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String users_id = (String) authentication.getPrincipal();

            postRequestDto.setUsers_id(users_id);

            // 📌 필요 시 요청 정보 사용 가능
            String clientIp = request.getRemoteAddr();
            System.out.println("📌 클라이언트 IP: " + clientIp);

            postService.createPost(postRequestDto);
            return ResponseEntity.ok("글 작성 완료");
        } catch (IllegalAccessException exception) {
            exception.printStackTrace();
            return ResponseEntity.internalServerError().body("업로드 중 오류 발생: " + exception.getMessage());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /* ===========================
       📌 조회 파트
       =========================== */
    @GetMapping
    public List<PostResponseDto> getPostsSorted(@RequestParam("sort") String sort,
                                                HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String users_id = (authentication != null && authentication.isAuthenticated())
                ? (String) authentication.getPrincipal()
                : null;

        return postService.getSortedPosts(sort, users_id);
    }

    @GetMapping("/cities")
    public List<PostResponseDto> getPostsByCity(@RequestParam("city") String city,
                                                HttpServletRequest request) {
        return postService.getPostsByCity(city);
    }

    @GetMapping("/all")
    public List<Post> getPostList(Model model, HttpServletRequest request) {
        List<Post> posts = postService.getAllPosts();
        model.addAttribute("posts", posts);
        return posts;
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostResponseDto> getPostById(@PathVariable int id,
                                                       HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String users_id = (authentication != null && authentication.isAuthenticated())
                ? (String) authentication.getPrincipal()
                : null;

        // 조회수 증가
//        postService.increaseViewCount(id);

        PostResponseDto post = postService.getPostById(id, users_id);
        return ResponseEntity.ok(post);
    }

    /* ===========================
       📌 수정 파트
       =========================== */
    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> update(@RequestBody PostRequestDto postRequestDto,
                                         @PathVariable int id,
                                         HttpServletRequest request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String users_id = (String) authentication.getPrincipal();

            postRequestDto.setUsers_id(users_id);

            postService.update(id, postRequestDto);
            return ResponseEntity.ok("수정 완료");
        } catch (IOException | IllegalAccessException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("수정 중 오류 발생: " + e.getMessage());
        }
    }
}
