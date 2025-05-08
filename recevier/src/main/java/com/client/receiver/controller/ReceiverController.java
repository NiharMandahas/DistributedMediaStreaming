package com.client.receiver.controller;


import com.client.receiver.component.UdpReceiver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/receiver")
public class ReceiverController {
    private static final Logger logger = LoggerFactory.getLogger(ReceiverController.class);

    @Value("${udp.receiver.output-path:${user.dir}/received/}")
    private String outputPath;

    @Autowired
    private UdpReceiver udpReceiver;

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("status", "running");

        // Get list of received files
        File outputDir = new File(outputPath);
        if (outputDir.exists() && outputDir.isDirectory()) {
            List<Map<String, Object>> files = Arrays.stream(outputDir.listFiles())
                    .filter(File::isFile)
                    .map(file -> {
                        Map<String, Object> fileInfo = new HashMap<>();
                        fileInfo.put("name", file.getName());
                        fileInfo.put("size", file.length());
                        fileInfo.put("lastModified", file.lastModified());
                        return fileInfo;
                    })
                    .collect(Collectors.toList());

            status.put("receivedFiles", files);
        }

        return ResponseEntity.ok(status);
    }
}