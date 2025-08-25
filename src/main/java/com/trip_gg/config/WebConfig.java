package com.trip_gg.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;                    // [신규] 로깅 추가
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

import java.nio.file.Files;                        // [변경] throws 제거 → try/catch로 처리
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * [Configuration] 정적 리소스(이미지) 매핑 설정
 *
 * ■ 핵심
 *  - [변경] 서버 디렉토리(file.upload.dir) + URL prefix(file.upload.uri-prefix) 기반으로 서빙
 *  - [변경] 디렉터리 자동 생성 실패 시 애플리케이션이 죽지 않도록 WARN 로그만 남기고 계속 진행
 *    -> 로컬에서 서버 경로(/home/gyubuntu/...)를 지정했을 때의 "Operation not supported" 회피
 *
 * 예시 환경변수)
 *   FILE_UPLOAD_DIR=/home/gyubuntu/project/media/awesomebros_uploads/
 *   FILE_UPLOAD_URI_PREFIX=/media/awesomebros_uploads/
 *   FILE_UPLOAD_AUTO_CREATE_DIR=true
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private static final Logger log = LoggerFactory.getLogger(WebConfig.class); // [신규]

    // [변경] 서버 파일 저장 루트 (기본값 예시: /var/app/uploads/)
    @Value("${file.upload.dir:/toyProject/uploads/}")
    private String uploadDir;

    // [변경] 외부 노출 URL prefix (기본값: /uploads/)
    @Value("${file.upload.uri-prefix:/uploads/}")
    private String uriPrefix;

    // [신규] 디렉터리 자동 생성 여부 (운영/개발 환경에서 선택적으로 끌 수 있음)
    @Value("${file.upload.auto-create-dir:true}")
    private boolean autoCreateDir;

    // [변경] 경로 정규화 및 디렉토리 생성(실패해도 종료하지 않음)
    @PostConstruct
    public void init() {
        // prefix 정규화: 앞/뒤 슬래시 보장
        if (uriPrefix == null || uriPrefix.isBlank()) {
            uriPrefix = "/uploads/";
        }
        if (!uriPrefix.startsWith("/")) uriPrefix = "/" + uriPrefix;
        if (!uriPrefix.endsWith("/")) uriPrefix = uriPrefix + "/";

        // 디렉토리 정규화: 절대경로 + 마지막 슬래시 보장
        Path base = Paths.get(uploadDir).toAbsolutePath().normalize();
        String normalized = base.toString().replace("\\", "/");
        if (!normalized.endsWith("/")) normalized = normalized + "/";
        this.uploadDir = normalized;

        // 디렉토리 자동 생성 (옵션)
        if (autoCreateDir) {
            try {
                Files.createDirectories(base);
            } catch (Exception e) { // [변경] 실패 시 애플리케이션 종료 대신 경고만 남김
                log.warn("⚠ 파일 업로드 디렉토리 생성 실패: path='{}', reason='{}' — 계속 진행합니다. (로컬에서 원격 경로 지정 시 정상적인 경고일 수 있음)",
                        base, e.getMessage());
            }
        } else {
            log.info("파일 업로드 디렉토리 자동 생성 비활성화됨(file.upload.auto-create-dir=false). path='{}'", base);
        }
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 예: /media/awesomebros_uploads/** → file:/home/gyubuntu/project/media/awesomebros_uploads/
        registry.addResourceHandler(uriPrefix + "**")
                .addResourceLocations("file:" + uploadDir)
                .setCachePeriod(3600)
                .resourceChain(true)
                .addResolver(new PathResourceResolver());
        log.info("Static resource mapping: '{}' -> 'file:{}'", (uriPrefix + "**"), uploadDir);
    }
}