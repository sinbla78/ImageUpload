package com.example.imageUpload.controller;

import com.example.imageUpload.service.ImageUploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/images")
@CrossOrigin(origins = "*") // 개발용, 프로덕션에서는 특정 도메인으로 제한
public class ImageUploadController {

    @Autowired
    private ImageUploadService imageUploadService;

    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadImage(@RequestParam("file") MultipartFile file) {
        Map<String, Object> response = new HashMap<>();

        try {
            // 파일 검증
            if (file.isEmpty()) {
                response.put("success", false);
                response.put("message", "파일이 비어있습니다.");
                return ResponseEntity.badRequest().body(response);
            }

            // 이미지 파일 업로드 처리
            String fileName = imageUploadService.uploadImage(file);

            response.put("success", true);
            response.put("message", "이미지가 성공적으로 업로드되었습니다.");
            response.put("fileName", fileName);
            response.put("fileUrl", "/api/images/view/" + fileName);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "이미지 업로드 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/view/{fileName}")
    public ResponseEntity<byte[]> viewImage(@PathVariable String fileName) {
        try {
            return imageUploadService.getImage(fileName);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/delete/{fileName}")
    public ResponseEntity<Map<String, Object>> deleteImage(@PathVariable String fileName) {
        Map<String, Object> response = new HashMap<>();

        try {
            boolean deleted = imageUploadService.deleteImage(fileName);

            if (deleted) {
                response.put("success", true);
                response.put("message", "이미지가 성공적으로 삭제되었습니다.");
            } else {
                response.put("success", false);
                response.put("message", "이미지를 찾을 수 없습니다.");
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "이미지 삭제 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}