package com.example.p2pfileshare.controller;

import com.example.p2pfileshare.model.FileMetadata;
import com.example.p2pfileshare.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/files")
public class FileController {

    @Autowired
    private FileService fileService;

    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("uploaderId") String uploaderId) {

        try {
            String fileId = fileService.uploadFile(file, uploaderId);
            return ResponseEntity.ok(Map.of(
                    "fileId", fileId,
                    "message", "File uploaded and distributed successfully"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/download/{fileId}")
    public ResponseEntity<ByteArrayResource> downloadFile(@PathVariable String fileId) {
        try {
            Optional<FileMetadata> metadataOpt = fileService.getFileMetadata(fileId);
            if (metadataOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            FileMetadata metadata = metadataOpt.get();
            byte[] fileData = fileService.downloadFile(fileId);

            ByteArrayResource resource = new ByteArrayResource(fileData);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + metadata.getFileName() + "\"")
                    .contentType(MediaType.parseMediaType(metadata.getContentType()))
                    .contentLength(fileData.length)
                    .body(resource);

        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/list")
    public ResponseEntity<List<FileMetadata>> getAllFiles() {
        return ResponseEntity.ok(fileService.getAllFiles());
    }

    @GetMapping("/videos")
    public ResponseEntity<List<FileMetadata>> getVideoFiles() {
        return ResponseEntity.ok(fileService.getVideoFiles());
    }

    @GetMapping("/metadata/{fileId}")
    public ResponseEntity<FileMetadata> getFileMetadata(@PathVariable String fileId) {
        Optional<FileMetadata> metadata = fileService.getFileMetadata(fileId);
        return metadata.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }
}
