package controller;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@RestController
@RequestMapping("/peer")
public class PeerController {

    @Value("${peers.list}")
    private String peerList;  // Comma-separated peer URLs

    // Inject the server port from the application context
    private final ServletWebServerApplicationContext server;

    private final RestTemplate restTemplate = new RestTemplate();

    public PeerController(ServletWebServerApplicationContext server) {
        this.server = server;
    }

    /**
     * Query all peers to check if a file exists
     * @param filename The file to search for
     * @return Map containing results from all peers
     */
    @GetMapping("/search")
    @JsonIgnore
    public Map<String, Boolean> searchFileAcrossPeers(@RequestParam String filename) {
        Map<String, Boolean> resultMap = new HashMap<>();

        if (peerList == null || peerList.trim().isEmpty()) {
            return resultMap;
        }

        for (String peer : peerList.split(",")) {
            try {
                if (peer.trim().isEmpty()) continue;

                // Skip self if URL points to this instance
//                if (isSelf(peer)) continue;

                System.out.println("Querying peer: " + peer);

                Boolean hasFile = restTemplate.getForObject(
                        peer + "/peer/hasfile?filename=" + filename, Boolean.class
                );

                resultMap.put(peer, hasFile != null && hasFile);
            } catch (Exception e) {
                System.out.println("Error connecting to peer " + peer + ": " + e.getMessage());
                resultMap.put(peer, false); // timeout or unreachable peer
            }
        }

        return resultMap;
    }

    /**
     * Endpoint for peers to check if a file exists on this server
     * @param filename The file to check for
     * @return true if the file exists, false otherwise
     */
    @GetMapping("/hasfile")
    @JsonIgnore
    public boolean hasFile(@RequestParam String filename) {
        // Get the absolute path of the storage directory
        String storagePath = System.getProperty("user.dir") + "/storage/";

        // Create a File object with the full path
        java.io.File file = new java.io.File(storagePath + filename);

        // Return true if the file exists and is a file (not a directory)
        boolean exists = file.exists() && file.isFile();
        System.out.println("Peer check for file: " + storagePath + filename + " - Exists: " + exists);
        return exists;
    }

    /**
     * Determines if a given peer URL resolves to the current instance
     * @param peerUrl The peer URL to check
     * @return true if the peer is this instance, false otherwise
     */
    private boolean isSelf(String peerUrl) {
        try {
            // Get local port from the server context
            int localPort = server.getWebServer().getPort();

            // Extract host and port from the peer URL
            String peerUrlLower = peerUrl.toLowerCase();
            String peerHost;
            int peerPort;

            if (peerUrlLower.startsWith("http://") || peerUrlLower.startsWith("https://")) {
                String urlWithoutProtocol = peerUrl.substring(peerUrl.indexOf("://") + 3);
                String[] hostPortParts = urlWithoutProtocol.split(":", 2);
                peerHost = hostPortParts[0];
                peerPort = hostPortParts.length > 1 ? Integer.parseInt(hostPortParts[1]) : 80;
            } else {
                String[] hostPortParts = peerUrl.split(":", 2);
                peerHost = hostPortParts[0];
                peerPort = hostPortParts.length > 1 ? Integer.parseInt(hostPortParts[1]) : 80;
            }

            // Check if the URL has localhost or 127.0.0.1
            boolean isLocalhost = peerHost.equals("localhost") || peerHost.equals("127.0.0.1");

            // Get local IP address for comparison with non-localhost addresses
            java.net.InetAddress localAddress = java.net.InetAddress.getLocalHost();
            String localIp = localAddress.getHostAddress();

            // Check if both IP and port match (this is the key fix - we now check ports too)
            boolean isSelf = (isLocalhost || peerHost.equals(localIp)) && (peerPort == localPort);

            if (isSelf) {
                System.out.println("Skipping self: " + peerUrl + " (local port: " + localPort + ")");
            }
            return isSelf;
        } catch (Exception e) {
            // If there's an error, assume it's not self (safe fallback)
            e.printStackTrace();
            return false;
        }
    }
}