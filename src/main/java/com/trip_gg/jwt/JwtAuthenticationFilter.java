package com.trip_gg.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // 1. 요청에서 AccessToken 추출
        String accessToken = jwtTokenProvider.resolveToken(request);

        try {
            // 2. AccessToken이 있고 유효하다면
            if (accessToken != null && jwtTokenProvider.validateToken(accessToken)) {

                // 3. 토큰에서 사용자 정보(username) 추출
                String username = jwtTokenProvider.getUsername(accessToken);

                // 4. 인증 객체 생성 및 SecurityContext에 저장
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(username, null, null);
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }

        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            // 🚨 Access Token이 만료된 경우
            System.out.println("Access Token 만료: " + e.getMessage());

            // 5. RefreshToken 쿠키에서 추출
            String refreshToken = getRefreshTokenFromCookie(request);

            // 6. RefreshToken 유효성 검사
            if (refreshToken != null && jwtTokenProvider.validateToken(refreshToken)) {
                // 7. RefreshToken에서 사용자 정보(username) 추출
                String username = jwtTokenProvider.getUsername(refreshToken);

                // 8. 새로운 AccessToken 발급
                String newAccessToken = jwtTokenProvider.generateToken(username);

                // 9. 새 AccessToken을 응답 헤더 또는 쿠키에 담아 전달
                response.setHeader("Authorization", "Bearer " + newAccessToken);

                // 📌 필요하다면 쿠키로도 내려줌
                Cookie accessTokenCookie = new Cookie("accessToken", newAccessToken);
                accessTokenCookie.setHttpOnly(true);
                accessTokenCookie.setPath("/");
                response.addCookie(accessTokenCookie);

                // 10. 인증 객체 생성 및 SecurityContext에 저장
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(username, null, null);
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);

            } else {
                // 🚨 RefreshToken도 없거나 만료된 경우 → 401 반환
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"error\":\"토큰이 만료되었습니다. 다시 로그인해주세요.\"}");
                return;
            }
        }

        // 11. 다음 필터로 넘기기
        filterChain.doFilter(request, response);
    }

    // 🔹 RefreshToken 쿠키 추출 메서드
    private String getRefreshTokenFromCookie(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("refreshToken".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}
