package com.example.p2pfileshare.repository;

import com.example.p2pfileshare.model.FileMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface FileMetadataRepository extends JpaRepository<FileMetadata, Long> {
    Optional<FileMetadata> findByFileId(String fileId);
    List<FileMetadata> findByUploaderId(String uploaderId);
    List<FileMetadata> findByIsVideo(Boolean isVideo);
    List<FileMetadata> findByContentTypeContaining(String contentType);
}
