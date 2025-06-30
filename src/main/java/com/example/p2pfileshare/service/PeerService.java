package com.example.p2pfileshare.service;

import com.example.p2pfileshare.model.Peer;
import com.example.p2pfileshare.repository.PeerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class PeerService {

    @Autowired
    private PeerRepository peerRepository;

    public String registerPeer(String ipAddress, Integer port, Boolean isHost) {
        String peerId = UUID.randomUUID().toString();
        Peer peer = new Peer(peerId, ipAddress, port, isHost);
        peerRepository.save(peer);
        return peerId;
    }

    public void updatePeerStatus(String peerId, Boolean isOnline) {
        Optional<Peer> peerOpt = peerRepository.findByPeerId(peerId);
        if (peerOpt.isPresent()) {
            Peer peer = peerOpt.get();
            peer.setIsOnline(isOnline);
            peer.setLastSeen(LocalDateTime.now());
            peerRepository.save(peer);
        }
    }

    public List<Peer> getOnlinePeers() {
        return peerRepository.findByIsOnline(true);
    }

    public List<Peer> getAllPeers() {
        return peerRepository.findAll();
    }

    public Optional<Peer> getPeer(String peerId) {
        return peerRepository.findByPeerId(peerId);
    }

    public void disconnectPeer(String peerId) {
        updatePeerStatus(peerId, false);
    }

    public List<Peer> getHostPeers() {
        return peerRepository.findByIsHost(true);
    }
}
