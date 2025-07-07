package com.trip_gg.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ImageService {
    private final String UPLOAD_BASE_DIR = System.getProperty("user.dir");
    private final String UPLOAD_SUB_DIR = "/uploads/temp/";

    public String uploadImage(MultipartFile file) throws IOException{
        Path uploadPath = Paths.get(UPLOAD_BASE_DIR + UPLOAD_SUB_DIR);

        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        String originalFilename = Objects.requireNonNull(file.getOriginalFilename());
        String timestamp = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());
        String newFileName = timestamp + "_" + originalFilename;

        Path filePath = uploadPath.resolve(newFileName);
        file.transferTo(filePath.toFile());

        String fileUrl = "http://localhost:8080" + UPLOAD_SUB_DIR + newFileName;

        return fileUrl;
    }
}