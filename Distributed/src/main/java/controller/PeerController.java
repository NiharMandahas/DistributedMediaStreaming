package controller;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
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
    public Map<String, Object> searchFileAcrossPeers(@RequestParam String filename) {
        Map<String, Object> resultMap = new HashMap<>();

        if (peerList == null || peerList.trim().isEmpty()) {
            return resultMap;
        }

        for (String peer : peerList.split(",")) {
            try {
                if (peer.trim().isEmpty()) continue;

                // Skip self if URL points to this instance
//                if (isSelf(peer)) continue;

                System.out.println("Querying peer: " + peer);

                // The response could now be either a Boolean or a file download
                try {
                    Boolean hasFile = restTemplate.getForObject(
                            peer + "/peer/hasfile?filename=" + filename, Boolean.class
                    );
                    resultMap.put(peer, hasFile != null && hasFile);
                } catch (Exception e) {
                    // If we get here, it might be because the response is not a boolean
                    // This could mean the file was found and is being returned
                    resultMap.put(peer, "File found and available for download at: " +
                            peer + "/peer/hasfile?filename=" + filename);
                }
            } catch (Exception e) {
                System.out.println("Error connecting to peer " + peer + ": " + e.getMessage());
                resultMap.put(peer, false); // timeout or unreachable peer
            }
        }

        return resultMap;
    }

    /**
     * Endpoint for peers to check if a file exists on this server
     * and return the file if it exists
     * @param filename The file to check for
     * @return the file as a download if it exists, or false if it doesn't
     */
    @GetMapping("/hasfile")
    @JsonIgnore
    public ResponseEntity<?> hasFile(@RequestParam String filename) {
        // Get the absolute path of the storage directory
        String storagePath = System.getProperty("user.dir") + "/storage/";

        // Create a File object with the full path
        java.io.File file = new java.io.File(storagePath + filename);

        // Check if the file exists and is a file (not a directory)
        boolean exists = file.exists() && file.isFile();
        System.out.println("Peer check for file: " + storagePath + filename + " - Exists: " + exists);

        if (exists) {
            return downloadFile(filename);
        } else {
            return ResponseEntity.ok(false);
        }
    }

    /**
     * Download a file from the local server if it exists
     * @param filename The file to download
     * @return The file as a downloadable resource or an error response
     */
    @GetMapping("/download")
    public ResponseEntity<?> downloadFile(@RequestParam String filename) {
        try {
            // Get the absolute path of the storage directory
            String storagePath = System.getProperty("user.dir") + "/storage/";
            Path filePath = Paths.get(storagePath + filename);

            // Check if file exists
            java.io.File file = filePath.toFile();
            if (!file.exists() || !file.isFile()) {
                return ResponseEntity.notFound().build();
            }

            // Create resource from file
            Resource resource = new UrlResource(filePath.toUri());

            // Try to determine file's content type
            String contentType = determineContentType(filename);

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getName() + "\"")
                    .body(resource);

        } catch (MalformedURLException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    /**
     * Determine content type based on file extension
     * @param filename The filename to check
     * @return The content type as a string
     */
    private String determineContentType(String filename) {
        if (filename.toLowerCase().endsWith(".pdf")) {
            return "application/pdf";
        } else if (filename.toLowerCase().endsWith(".jpg") || filename.toLowerCase().endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (filename.toLowerCase().endsWith(".png")) {
            return "image/png";
        } else if (filename.toLowerCase().endsWith(".txt")) {
            return "text/plain";
        } else if (filename.toLowerCase().endsWith(".html") || filename.toLowerCase().endsWith(".htm")) {
            return "text/html";
        } else if (filename.toLowerCase().endsWith(".mp4")) {
            return "video/mp4";
        } else if (filename.toLowerCase().endsWith(".mp3")) {
            return "audio/mpeg";
        } else {
            return "application/octet-stream"; // Default binary file type
        }
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