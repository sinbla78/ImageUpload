package com.example.imageUpload.controller;

import com.example.imageUpload.service.ImageUploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/profile")
@CrossOrigin(origins = "*") // 개발용, 프로덕션에서는 특정 도메인으로 제한
public class ImageUploadController {

    @Autowired
    private ImageUploadService imageUploadService;

    /**
     * 프로필 이미지 업로드
     */
    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadProfileImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "userId", required = false) String userId) {

        Map<String, Object> response = new HashMap<>();

        try {
            // 파일 검증
            if (file.isEmpty()) {
                response.put("success", false);
                response.put("message", "파일이 비어있습니다.");
                return ResponseEntity.badRequest().body(response);
            }

            // 프로필 이미지 업로드 처리
            String fileName = imageUploadService.uploadProfileImage(file, userId);

            response.put("success", true);
            response.put("message", "프로필 이미지가 성공적으로 업로드되었습니다.");
            response.put("fileName", fileName);
            response.put("fileUrl", "/api/profile/image/" + fileName);

            if (userId != null && !userId.trim().isEmpty()) {
                response.put("userProfileUrl", "/api/profile/user/" + userId);
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "프로필 이미지 업로드 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 파일명으로 이미지 조회
     */
    @GetMapping("/image/{fileName}")
    public ResponseEntity<byte[]> getImageByFileName(@PathVariable String fileName) {
        try {
            return imageUploadService.getImage(fileName);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 사용자 ID로 프로필 이미지 조회
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<byte[]> getUserProfileImage(@PathVariable String userId) {
        try {
            return imageUploadService.getUserProfileImage(userId);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 파일명으로 이미지 삭제
     */
    @DeleteMapping("/delete/{fileName}")
    public ResponseEntity<Map<String, Object>> deleteImageByFileName(@PathVariable String fileName) {
        Map<String, Object> response = new HashMap<>();

        try {
            boolean deleted = imageUploadService.deleteImage(fileName);

            if (deleted) {
                response.put("success", true);
                response.put("message", "프로필 이미지가 성공적으로 삭제되었습니다.");
            } else {
                response.put("success", false);
                response.put("message", "프로필 이미지를 찾을 수 없습니다.");
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "프로필 이미지 삭제 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 사용자의 프로필 이미지 삭제
     */
    @DeleteMapping("/user/{userId}")
    public ResponseEntity<Map<String, Object>> deleteUserProfileImage(@PathVariable String userId) {
        Map<String, Object> response = new HashMap<>();

        try {
            boolean deleted = imageUploadService.deleteUserProfileImage(userId);

            if (deleted) {
                response.put("success", true);
                response.put("message", "사용자의 프로필 이미지가 성공적으로 삭제되었습니다.");
            } else {
                response.put("success", false);
                response.put("message", "사용자의 프로필 이미지를 찾을 수 없습니다.");
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "프로필 이미지 삭제 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 서버 상태 확인
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "OK");
        response.put("message", "프로필 이미지 서버가 정상 작동 중입니다.");
        response.put("service", "Profile Image Upload Service");
        return ResponseEntity.ok(response);
    }
}