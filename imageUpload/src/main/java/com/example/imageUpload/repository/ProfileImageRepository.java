package com.example.imageUpload.repository;


import com.example.imageUpload.entity.ProfileImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProfileImageRepository extends JpaRepository<ProfileImage, Long> {

    // 파일명으로 이미지 찾기
    Optional<ProfileImage> findByFileName(String fileName);

    // 사용자 ID로 프로필 이미지 찾기
    Optional<ProfileImage> findByUserId(String userId);

    // 사용자 ID로 모든 이미지 찾기 (이력 관리용)
    List<ProfileImage> findByUserIdOrderByUploadedAtDesc(String userId);

    // 파일명 존재 여부 확인
    boolean existsByFileName(String fileName);

    // 사용자의 최신 프로필 이미지만 조회 (imageData 제외로 메모리 효율성)
    @Query("SELECT p.id, p.fileName, p.originalFileName, p.contentType, p.fileSize, p.uploadedAt, p.userId " +
            "FROM ProfileImage p WHERE p.userId = :userId ORDER BY p.uploadedAt DESC")
    List<Object[]> findProfileMetadataByUserId(String userId);
}