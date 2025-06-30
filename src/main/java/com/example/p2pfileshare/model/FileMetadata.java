package com.example.p2pfileshare.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "file_metadata")
public class FileMetadata {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String fileId;

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false)
    private String contentType;

    @Column(nullable = false)
    private Long fileSize;

    @Column(nullable = false)
    private Integer totalChunks;

    @Column(nullable = false)
    private String uploaderId;

    @Column(nullable = false)
    private LocalDateTime uploadTime;

    @Column(nullable = false)
    private Boolean isVideo;

    @OneToMany(mappedBy = "fileMetadata", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ChunkLocation> chunkLocations;

    // Constructors
    public FileMetadata() {}

    public FileMetadata(String fileId, String fileName, String contentType, Long fileSize,
                        Integer totalChunks, String uploaderId, Boolean isVideo) {
        this.fileId = fileId;
        this.fileName = fileName;
        this.contentType = contentType;
        this.fileSize = fileSize;
        this.totalChunks = totalChunks;
        this.uploaderId = uploaderId;
        this.uploadTime = LocalDateTime.now();
        this.isVideo = isVideo;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFileId() { return fileId; }
    public void setFileId(String fileId) { this.fileId = fileId; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }

    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }

    public Integer getTotalChunks() { return totalChunks; }
    public void setTotalChunks(Integer totalChunks) { this.totalChunks = totalChunks; }

    public String getUploaderId() { return uploaderId; }
    public void setUploaderId(String uploaderId) { this.uploaderId = uploaderId; }

    public LocalDateTime getUploadTime() { return uploadTime; }
    public void setUploadTime(LocalDateTime uploadTime) { this.uploadTime = uploadTime; }

    public Boolean getIsVideo() { return isVideo; }
    public void setIsVideo(Boolean isVideo) { this.isVideo = isVideo; }

    public List<ChunkLocation> getChunkLocations() { return chunkLocations; }
    public void setChunkLocations(List<ChunkLocation> chunkLocations) { this.chunkLocations = chunkLocations; }
}
