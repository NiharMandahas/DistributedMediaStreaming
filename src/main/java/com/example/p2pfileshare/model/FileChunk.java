package com.example.p2pfileshare.model;

public class FileChunk {
    private String fileId;
    private Integer chunkIndex;
    private byte[] data;
    private Long size;
    private String checksum;

    public FileChunk() {}

    public FileChunk(String fileId, Integer chunkIndex, byte[] data, Long size, String checksum) {
        this.fileId = fileId;
        this.chunkIndex = chunkIndex;
        this.data = data;
        this.size = size;
        this.checksum = checksum;
    }

    // Getters and Setters
    public String getFileId() { return fileId; }
    public void setFileId(String fileId) { this.fileId = fileId; }

    public Integer getChunkIndex() { return chunkIndex; }
    public void setChunkIndex(Integer chunkIndex) { this.chunkIndex = chunkIndex; }

    public byte[] getData() { return data; }
    public void setData(byte[] data) { this.data = data; }

    public Long getSize() { return size; }
    public void setSize(Long size) { this.size = size; }

    public String getChecksum() { return checksum; }
    public void setChecksum(String checksum) { this.checksum = checksum; }
}
