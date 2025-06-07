package com.example.imageUpload.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class ImageUploadService {

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    private final List<String> allowedExtensions = Arrays.asList("jpg", "jpeg", "png", "gif", "bmp", "webp");
    private final long maxFileSize = 5 * 1024 * 1024; // 5MB

    public String uploadImage(MultipartFile file) throws Exception {
        // 파일 검증
        validateImageFile(file);

        // 업로드 디렉토리 생성
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // 고유한 파일명 생성
        String originalFileName = file.getOriginalFilename();
        String extension = getFileExtension(originalFileName);
        String uniqueFileName = UUID.randomUUID().toString() + "." + extension;

        // 파일 저장
        Path filePath = uploadPath.resolve(uniqueFileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        return uniqueFileName;
    }

    public ResponseEntity<byte[]> getImage(String fileName) throws IOException {
        Path filePath = Paths.get(uploadDir).resolve(fileName);

        if (!Files.exists(filePath)) {
            throw new IOException("파일을 찾을 수 없습니다: " + fileName);
        }

        byte[] imageBytes = Files.readAllBytes(filePath);

        // Content-Type 설정
        String contentType = Files.probeContentType(filePath);
        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(contentType));

        return ResponseEntity.ok()
                .headers(headers)
                .body(imageBytes);
    }

    public boolean deleteImage(String fileName) {
        try {
            Path filePath = Paths.get(uploadDir).resolve(fileName);
            return Files.deleteIfExists(filePath);
        } catch (IOException e) {
            return false;
        }
    }

    private void validateImageFile(MultipartFile file) throws Exception {
        // 파일 크기 검증
        if (file.getSize() > maxFileSize) {
            throw new Exception("파일 크기가 5MB를 초과합니다.");
        }

        // 파일 확장자 검증
        String fileName = file.getOriginalFilename();
        if (fileName == null || fileName.isEmpty()) {
            throw new Exception("파일명이 유효하지 않습니다.");
        }

        String extension = getFileExtension(fileName).toLowerCase();
        if (!allowedExtensions.contains(extension)) {
            throw new Exception("허용되지 않는 파일 형식입니다. 허용 형식: " + String.join(", ", allowedExtensions));
        }

        // MIME 타입 검증
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new Exception("이미지 파일만 업로드 가능합니다.");
        }
    }

    private String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return "";
        }
        return fileName.substring(lastDotIndex + 1);
    }
}