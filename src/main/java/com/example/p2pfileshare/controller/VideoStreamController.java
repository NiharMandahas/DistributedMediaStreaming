package com.example.p2pfileshare.controller;

import com.example.p2pfileshare.service.VideoStreamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/stream")
public class VideoStreamController {

    @Autowired
    private VideoStreamService videoStreamService;

    @GetMapping("/video/{fileId}")
    public ResponseEntity<InputStreamResource> streamVideo(
            @PathVariable String fileId,
            @RequestHeader(value = "Range", required = false) String rangeHeader) {

        try {
            return videoStreamService.streamVideo(fileId, rangeHeader);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
