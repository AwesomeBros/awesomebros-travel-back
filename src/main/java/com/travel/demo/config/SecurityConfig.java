package com.travel.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf().disable() // 🔸 POST 테스트 시 CSRF 방지
                .authorizeHttpRequests(authorize -> authorize
                        .anyRequest().permitAll() // 🔥 인증 없이 전체 허용
                );
        return http.build();
    }
}