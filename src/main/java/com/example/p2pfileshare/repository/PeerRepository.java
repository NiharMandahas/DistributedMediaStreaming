package com.example.p2pfileshare.repository;

import com.example.p2pfileshare.model.Peer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface PeerRepository extends JpaRepository<Peer, Long> {
    Optional<Peer> findByPeerId(String peerId);
    List<Peer> findByIsOnline(Boolean isOnline);
    List<Peer> findByIsHost(Boolean isHost);
}
