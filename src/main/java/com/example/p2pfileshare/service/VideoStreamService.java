package com.example.p2pfileshare.service;

import com.example.p2pfileshare.model.FileMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Optional;

@Service
public class VideoStreamService {

    @Autowired
    private FileService fileService;

    public ResponseEntity<InputStreamResource> streamVideo(String fileId, String rangeHeader) throws IOException {
        Optional<FileMetadata> metadataOpt = fileService.getFileMetadata(fileId);

        if (metadataOpt.isEmpty() || !metadataOpt.get().getIsVideo()) {
            return ResponseEntity.notFound().build();
        }

        FileMetadata metadata = metadataOpt.get();
        byte[] videoData = fileService.downloadFile(fileId);

        long fileLength = videoData.length;
        long start = 0;
        long end = fileLength - 1;

        // Handle range requests for video streaming
        if (rangeHeader != null && rangeHeader.startsWith("bytes=")) {
            String[] ranges = rangeHeader.substring(6).split("-");
            try {
                start = Long.parseLong(ranges[0]);
                if (ranges.length > 1 && !ranges[1].isEmpty()) {
                    end = Long.parseLong(ranges[1]);
                }
            } catch (NumberFormatException e) {
                return ResponseEntity.status(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE).build();
            }
        }

        long contentLength = end - start + 1;
        byte[] rangeData = new byte[(int) contentLength];
        System.arraycopy(videoData, (int) start, rangeData, 0, (int) contentLength);

        InputStreamResource resource = new InputStreamResource(new ByteArrayInputStream(rangeData));

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Range", String.format("bytes %d-%d/%d", start, end, fileLength));
        headers.add("Accept-Ranges", "bytes");
        headers.setContentLength(contentLength);
        headers.setContentType(MediaType.parseMediaType(metadata.getContentType()));

        return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                .headers(headers)
                .body(resource);
    }
}
