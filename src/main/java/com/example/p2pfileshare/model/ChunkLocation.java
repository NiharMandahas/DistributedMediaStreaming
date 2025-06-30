package com.example.p2pfileshare.model;

import jakarta.persistence.*;

@Entity
@Table(name = "chunk_locations")
public class ChunkLocation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file_metadata_id")
    private FileMetadata fileMetadata;

    @Column(nullable = false)
    private Integer chunkIndex;

    @Column(nullable = false)
    private String peerId;

    @Column(nullable = false)
    private String chunkPath;

    @Column(nullable = false)
    private Long chunkSize;

    // Constructors
    public ChunkLocation() {}

    public ChunkLocation(FileMetadata fileMetadata, Integer chunkIndex, String peerId,
                         String chunkPath, Long chunkSize) {
        this.fileMetadata = fileMetadata;
        this.chunkIndex = chunkIndex;
        this.peerId = peerId;
        this.chunkPath = chunkPath;
        this.chunkSize = chunkSize;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public FileMetadata getFileMetadata() { return fileMetadata; }
    public void setFileMetadata(FileMetadata fileMetadata) { this.fileMetadata = fileMetadata; }

    public Integer getChunkIndex() { return chunkIndex; }
    public void setChunkIndex(Integer chunkIndex) { this.chunkIndex = chunkIndex; }

    public String getPeerId() { return peerId; }
    public void setPeerId(String peerId) { this.peerId = peerId; }

    public String getChunkPath() { return chunkPath; }
    public void setChunkPath(String chunkPath) { this.chunkPath = chunkPath; }

    public Long getChunkSize() { return chunkSize; }
    public void setChunkSize(Long chunkSize) { this.chunkSize = chunkSize; }
}
