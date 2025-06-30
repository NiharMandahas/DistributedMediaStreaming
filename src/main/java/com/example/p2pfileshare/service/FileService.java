package com.example.p2pfileshare.service;

import com.example.p2pfileshare.model.FileMetadata;
import com.example.p2pfileshare.model.FileChunk;
import com.example.p2pfileshare.model.Peer;
import com.example.p2pfileshare.repository.FileMetadataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class FileService {

    @Autowired
    private FileMetadataRepository fileMetadataRepository;

    @Autowired
    private ChunkService chunkService;

    @Autowired
    private PeerService peerService;

    public String uploadFile(MultipartFile file, String uploaderId) throws Exception {
        String fileId = UUID.randomUUID().toString();
        String fileName = file.getOriginalFilename();
        String contentType = file.getContentType();
        Long fileSize = file.getSize();
        Boolean isVideo = isVideoFile(contentType);

        // Split file into chunks
        List<FileChunk> chunks = chunkService.splitFileIntoChunks(fileId, file.getBytes());
        Integer totalChunks = chunks.size();

        // Create file metadata
        FileMetadata metadata = new FileMetadata(fileId, fileName, contentType, fileSize, totalChunks, uploaderId, isVideo);
        fileMetadataRepository.save(metadata);

        // Distribute chunks among peers
        distributeChunks(metadata, chunks, uploaderId);

        return fileId;
    }

    private void distributeChunks(FileMetadata metadata, List<FileChunk> chunks, String uploaderId) throws IOException {
        List<Peer> onlinePeers = peerService.getOnlinePeers();

        if (onlinePeers.isEmpty()) {
            throw new RuntimeException("No online peers available for file distribution");
        }

        // Distribute chunks in round-robin fashion
        for (int i = 0; i < chunks.size(); i++) {
            FileChunk chunk = chunks.get(i);
            Peer targetPeer = onlinePeers.get(i % onlinePeers.size());

            // Save chunk to peer's storage
            chunkService.saveChunk(chunk, targetPeer.getPeerId());

            // Record chunk location
            String chunkPath = String.format("chunks/%s/chunk_%d_%s.dat",
                    metadata.getFileId(), i, targetPeer.getPeerId());
            chunkService.saveChunkLocation(metadata, i, targetPeer.getPeerId(), chunkPath, chunk.getSize());
        }
    }

    public byte[] downloadFile(String fileId) throws IOException {
        Optional<FileMetadata> metadataOpt = fileMetadataRepository.findByFileId(fileId);

        if (metadataOpt.isEmpty()) {
            throw new RuntimeException("File not found: " + fileId);
        }

        FileMetadata metadata = metadataOpt.get();

        // Check if uploader (host) is online
        Optional<Peer> uploaderPeer = peerService.getPeer(metadata.getUploaderId());
        if (uploaderPeer.isPresent() && !uploaderPeer.get().getIsOnline()) {
            // Host is offline, try to reassemble from distributed chunks
            return chunkService.reassembleFile(fileId, metadata.getTotalChunks());
        } else {
            // Host is online, get file directly from host
            return getFileFromHost(fileId, metadata);
        }
    }

    private byte[] getFileFromHost(String fileId, FileMetadata metadata) throws IOException {
        // In a real implementation, this would make an HTTP request to the host peer
        // For now, we'll reassemble from chunks as fallback
        return chunkService.reassembleFile(fileId, metadata.getTotalChunks());
    }

    public List<FileMetadata> getAllFiles() {
        return fileMetadataRepository.findAll();
    }

    public List<FileMetadata> getVideoFiles() {
        return fileMetadataRepository.findByIsVideo(true);
    }

    public Optional<FileMetadata> getFileMetadata(String fileId) {
        return fileMetadataRepository.findByFileId(fileId);
    }

    private boolean isVideoFile(String contentType) {
        return contentType != null && contentType.startsWith("video/");
    }
}
