package com.example.p2pfileshare.service;

import com.example.p2pfileshare.model.FileChunk;
import com.example.p2pfileshare.model.FileMetadata;
import com.example.p2pfileshare.model.ChunkLocation;
import com.example.p2pfileshare.repository.ChunkLocationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ChunkService {

    @Autowired
    private ChunkLocationRepository chunkLocationRepository;

    @Value("${file.storage.location}")
    private String storageLocation;

    @Value("${file.chunk.size}")
    private Long chunkSize;

    public List<FileChunk> splitFileIntoChunks(String fileId, byte[] fileData) throws Exception {
        List<FileChunk> chunks = new ArrayList<>();
        int totalChunks = (int) Math.ceil((double) fileData.length / chunkSize);

        for (int i = 0; i < totalChunks; i++) {
            int start = (int) (i * chunkSize);
            int end = (int) Math.min(start + chunkSize, fileData.length);
            byte[] chunkData = new byte[end - start];
            System.arraycopy(fileData, start, chunkData, 0, end - start);

            String checksum = calculateChecksum(chunkData);
            FileChunk chunk = new FileChunk(fileId, i, chunkData, (long) chunkData.length, checksum);
            chunks.add(chunk);
        }

        return chunks;
    }

    public void saveChunk(FileChunk chunk, String peerId) throws IOException {
        Path chunkDir = Paths.get(storageLocation, "chunks", chunk.getFileId());
        Files.createDirectories(chunkDir);

        String chunkFileName = String.format("chunk_%d_%s.dat", chunk.getChunkIndex(), peerId);
        Path chunkPath = chunkDir.resolve(chunkFileName);

        Files.write(chunkPath, chunk.getData());
    }

    public void saveChunkLocation(FileMetadata fileMetadata, Integer chunkIndex, String peerId, String chunkPath, Long chunkSize) {
        ChunkLocation location = new ChunkLocation(fileMetadata, chunkIndex, peerId, chunkPath, chunkSize);
        chunkLocationRepository.save(location);
    }

    public Optional<FileChunk> getChunk(String fileId, Integer chunkIndex) throws IOException {
        Optional<ChunkLocation> locationOpt = chunkLocationRepository.findByFileMetadataFileIdAndChunkIndex(fileId, chunkIndex);

        if (locationOpt.isPresent()) {
            ChunkLocation location = locationOpt.get();
            Path chunkPath = Paths.get(location.getChunkPath());

            if (Files.exists(chunkPath)) {
                byte[] chunkData = Files.readAllBytes(chunkPath);
                String checksum = calculateChecksum(chunkData);
                return Optional.of(new FileChunk(fileId, chunkIndex, chunkData, (long) chunkData.length, checksum));
            }
        }

        return Optional.empty();
    }

    public byte[] reassembleFile(String fileId, Integer totalChunks) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        for (int i = 0; i < totalChunks; i++) {
            Optional<FileChunk> chunkOpt = getChunk(fileId, i);
            if (chunkOpt.isPresent()) {
                outputStream.write(chunkOpt.get().getData());
            } else {
                throw new IOException("Missing chunk: " + i + " for file: " + fileId);
            }
        }

        return outputStream.toByteArray();
    }

    public List<ChunkLocation> getChunkLocations(String fileId) {
        return chunkLocationRepository.findByFileMetadataFileId(fileId);
    }

    private String calculateChecksum(byte[] data) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] hash = md.digest(data);
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
