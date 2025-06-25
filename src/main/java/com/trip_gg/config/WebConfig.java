package com.trip_gg.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final String UPLOAD_BASE_DIR = System.getProperty("user.dir");
    private final String UPLOAD_MAPPING_PATH = "/uploads/temp/";
    private final String UPLOAD_FILE_SYSTEM_PATH = UPLOAD_BASE_DIR + "/uploads/temp/";

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler(UPLOAD_MAPPING_PATH + "**")
                .addResourceLocations("file:" + UPLOAD_FILE_SYSTEM_PATH);
    }
}