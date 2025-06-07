package com.example.imageUpload.service;

import com.example.imageUpload.entity.ProfileImage;
import com.example.imageUpload.repository.ProfileImageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ImageUploadService {

    @Autowired
    private ProfileImageRepository profileImageRepository;

    private final List<String> allowedExtensions = Arrays.asList("jpg", "jpeg", "png", "gif", "bmp", "webp");
    private final long maxFileSize = 3 * 1024 * 1024; // 3MB (프로필 사진용)

    /**
     * 프로필 이미지 업로드
     */
    public String uploadProfileImage(MultipartFile file, String userId) throws Exception {
        // 파일 검증
        validateImageFile(file);

        // 기존 프로필 이미지가 있다면 삭제 (사용자당 하나의 프로필 이미지만 유지)
        if (userId != null && !userId.trim().isEmpty()) {
            Optional<ProfileImage> existingImage = profileImageRepository.findByUserId(userId);
            existingImage.ifPresent(profileImageRepository::delete);
        }

        // 고유한 파일명 생성
        String originalFileName = file.getOriginalFilename();
        String extension = getFileExtension(originalFileName);
        String uniqueFileName = UUID.randomUUID().toString() + "." + extension;

        // ProfileImage 엔티티 생성 및 저장
        ProfileImage profileImage = new ProfileImage(
                uniqueFileName,
                originalFileName,
                file.getContentType(),
                file.getSize(),
                file.getBytes()
        );

        if (userId != null && !userId.trim().isEmpty()) {
            profileImage.setUserId(userId);
        }

        profileImageRepository.save(profileImage);
        return uniqueFileName;
    }

    /**
     * 이미지 조회
     */
    public ResponseEntity<byte[]> getImage(String fileName) throws Exception {
        Optional<ProfileImage> imageOpt = profileImageRepository.findByFileName(fileName);

        if (imageOpt.isEmpty()) {
            throw new Exception("이미지를 찾을 수 없습니다: " + fileName);
        }

        ProfileImage profileImage = imageOpt.get();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(profileImage.getContentType()));
        headers.setContentLength(profileImage.getFileSize());

        return ResponseEntity.ok()
                .headers(headers)
                .body(profileImage.getImageData());
    }

    /**
     * 사용자의 프로필 이미지 조회
     */
    public ResponseEntity<byte[]> getUserProfileImage(String userId) throws Exception {
        Optional<ProfileImage> imageOpt = profileImageRepository.findByUserId(userId);

        if (imageOpt.isEmpty()) {
            throw new Exception("사용자의 프로필 이미지를 찾을 수 없습니다: " + userId);
        }

        ProfileImage profileImage = imageOpt.get();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(profileImage.getContentType()));
        headers.setContentLength(profileImage.getFileSize());

        return ResponseEntity.ok()
                .headers(headers)
                .body(profileImage.getImageData());
    }

    /**
     * 이미지 삭제
     */
    public boolean deleteImage(String fileName) {
        try {
            Optional<ProfileImage> imageOpt = profileImageRepository.findByFileName(fileName);
            if (imageOpt.isPresent()) {
                profileImageRepository.delete(imageOpt.get());
                return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 사용자의 프로필 이미지 삭제
     */
    public boolean deleteUserProfileImage(String userId) {
        try {
            Optional<ProfileImage> imageOpt = profileImageRepository.findByUserId(userId);
            if (imageOpt.isPresent()) {
                profileImageRepository.delete(imageOpt.get());
                return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 모든 프로필 이미지 목록 조회 (관리자용)
     */
    public List<ProfileImage> getAllProfileImages() {
        return profileImageRepository.findAll();
    }

    /**
     * 이미지 파일 검증
     */
    private void validateImageFile(MultipartFile file) throws Exception {
        // 파일 크기 검증
        if (file.getSize() > maxFileSize) {
            throw new Exception("파일 크기가 3MB를 초과합니다. (프로필 사진 권장 크기)");
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

    /**
     * 파일 확장자 추출
     */
    private String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return "";
        }
        return fileName.substring(lastDotIndex + 1);
    }
}