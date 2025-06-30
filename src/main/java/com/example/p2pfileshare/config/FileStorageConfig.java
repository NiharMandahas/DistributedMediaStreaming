package com.example.p2pfileshare.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@Configuration
public class FileStorageConfig {

    @Value("${file.storage.location}")
    private String storageLocation;

    @PostConstruct
    public void init() throws IOException {
        Files.createDirectories(Paths.get(storageLocation));
        Files.createDirectories(Paths.get(storageLocation, "chunks"));
    }
}
