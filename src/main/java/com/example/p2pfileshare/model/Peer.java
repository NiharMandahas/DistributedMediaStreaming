package com.example.p2pfileshare.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "peers")
public class Peer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String peerId;

    @Column(nullable = false)
    private String ipAddress;

    @Column(nullable = false)
    private Integer port;

    @Column(nullable = false)
    private Boolean isOnline;

    @Column(nullable = false)
    private LocalDateTime lastSeen;

    @Column(nullable = false)
    private Boolean isHost;

    // Constructors
    public Peer() {}

    public Peer(String peerId, String ipAddress, Integer port, Boolean isHost) {
        this.peerId = peerId;
        this.ipAddress = ipAddress;
        this.port = port;
        this.isOnline = true;
        this.lastSeen = LocalDateTime.now();
        this.isHost = isHost;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getPeerId() { return peerId; }
    public void setPeerId(String peerId) { this.peerId = peerId; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public Integer getPort() { return port; }
    public void setPort(Integer port) { this.port = port; }

    public Boolean getIsOnline() { return isOnline; }
    public void setIsOnline(Boolean isOnline) { this.isOnline = isOnline; }

    public LocalDateTime getLastSeen() { return lastSeen; }
    public void setLastSeen(LocalDateTime lastSeen) { this.lastSeen = lastSeen; }

    public Boolean getIsHost() { return isHost; }
    public void setIsHost(Boolean isHost) { this.isHost = isHost; }
}
