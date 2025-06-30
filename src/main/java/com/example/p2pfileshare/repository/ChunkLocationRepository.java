package com.example.p2pfileshare.repository;

import com.example.p2pfileshare.model.ChunkLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ChunkLocationRepository extends JpaRepository<ChunkLocation, Long> {
    List<ChunkLocation> findByFileMetadataFileId(String fileId);
    List<ChunkLocation> findByPeerId(String peerId);
    Optional<ChunkLocation> findByFileMetadataFileIdAndChunkIndex(String fileId, Integer chunkIndex);
    List<ChunkLocation> findByFileMetadataFileIdAndPeerIdIn(String fileId, List<String> peerIds);
}
