package com.example.p2pfileshare.controller;

import com.example.p2pfileshare.model.Peer;
import com.example.p2pfileshare.service.PeerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/peers")
public class PeerController {

    @Autowired
    private PeerService peerService;

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> registerPeer(
            @RequestBody Map<String, Object> request,
            HttpServletRequest httpRequest) {

        String ipAddress = httpRequest.getRemoteAddr();
        Integer port = (Integer) request.getOrDefault("port", 8080);
        Boolean isHost = (Boolean) request.getOrDefault("isHost", false);

        String peerId = peerService.registerPeer(ipAddress, port, isHost);

        return ResponseEntity.ok(Map.of("peerId", peerId, "message", "Peer registered successfully"));
    }

    @PostMapping("/{peerId}/status")
    public ResponseEntity<Map<String, String>> updatePeerStatus(
            @PathVariable String peerId,
            @RequestBody Map<String, Boolean> request) {

        Boolean isOnline = request.get("isOnline");
        peerService.updatePeerStatus(peerId, isOnline);

        return ResponseEntity.ok(Map.of("message", "Peer status updated"));
    }

    @GetMapping("/online")
    public ResponseEntity<List<Peer>> getOnlinePeers() {
        return ResponseEntity.ok(peerService.getOnlinePeers());
    }

    @GetMapping("/all")
    public ResponseEntity<List<Peer>> getAllPeers() {
        return ResponseEntity.ok(peerService.getAllPeers());
    }

    @DeleteMapping("/{peerId}")
    public ResponseEntity<Map<String, String>> disconnectPeer(@PathVariable String peerId) {
        peerService.disconnectPeer(peerId);
        return ResponseEntity.ok(Map.of("message", "Peer disconnected"));
    }
}
