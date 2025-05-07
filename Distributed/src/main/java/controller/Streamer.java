package controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.FileInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.io.File;

@RestController
@RequestMapping("/stream")
public class Streamer {

    @GetMapping("/play")
    public ResponseEntity<String> streamVideo(@RequestParam String videoName,
                                              @RequestParam String clientIp,
                                              @RequestParam int port) {
        String storagePath = System.getProperty("user.dir") + "/storage/";
        File videoFile = new File(storagePath + videoName);

        if (!videoFile.exists() || !videoFile.isFile()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("File not found!");
        }

        try (DatagramSocket socket = new DatagramSocket();
             FileInputStream fis = new FileInputStream(videoFile)) {

            InetAddress clientAddress = InetAddress.getByName(clientIp);
            byte[] buffer = new byte[1024];  // 1 KB packets
            int bytesRead;

            while ((bytesRead = fis.read(buffer)) != -1) {
                DatagramPacket packet = new DatagramPacket(buffer, bytesRead, clientAddress, port);
                socket.send(packet);
                Thread.sleep(10); // Optional: adjust to control packet flow rate
            }

            return ResponseEntity.ok("Video sent over UDP successfully.");

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error streaming video.");
        }
    }
}
