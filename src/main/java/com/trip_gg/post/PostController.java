package com.trip_gg.post;

import com.trip_gg.common.Pagination;
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
//            System.out.println("📌 클라이언트 IP: " + clientIp);

            postService.createPost(postRequestDto, request);
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

    // 상세 페이지
    @GetMapping("/{id}")
    public ResponseEntity<PostResponseDto> getPostById(@PathVariable("id") int id,
                                                       HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String users_id = (authentication != null && authentication.isAuthenticated())
                ? (String) authentication.getPrincipal()
                : null;

        // 조회수 증가
        postService.increaseViewCount(id);

        PostResponseDto post = postService.getPostById(id, users_id);
        return ResponseEntity.ok(post);
    }

    /* ===========================
       📌 수정 파트
       =========================== */
    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> update(@RequestBody PostRequestDto postRequestDto,
                                         @PathVariable("id") int id,
                                         HttpServletRequest request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String users_id = (String) authentication.getPrincipal();

            postRequestDto.setUsers_id(users_id);

            postService.update(id, postRequestDto, request);
            return ResponseEntity.ok("수정 완료");
        } catch (IOException | IllegalAccessException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("수정 중 오류 발생: " + e.getMessage());
        }
    }

//    @GetMapping("/search")
//    public ResponseEntity<List<PostResponseDto>> search(
//            @RequestParam(required = false) Integer countries_id,
//            @RequestParam(required = false) Integer cities_id,
//            @RequestParam(required = false) Integer districts_id,
//            HttpServletRequest request
//    ) {
//        List<PostResponseDto> result = postService.searchPosts(countries_id, cities_id, districts_id);
//        return ResponseEntity.ok(result);
//    }

    @GetMapping("/search")
    public ResponseEntity<Pagination<PostResponseDto>> searchByNames(
            @RequestParam(required = false) String country,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String district,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        List<PostResponseDto> all = postService.searchByNames(country, city, district);
        return ResponseEntity.ok(postService.paginate(all, page, size));
    }

//    @GetMapping("/cities-paged")
//    public ResponseEntity<Pagination<PostResponseDto>> getCityPaged(
//            @RequestParam String city,
//            @RequestParam(defaultValue = "1") int page,
//            @RequestParam(defaultValue = "10") int size
//    ) {
//        return ResponseEntity.ok(postService.getPostsByCityPaged(city, page, size));
//    }
}
