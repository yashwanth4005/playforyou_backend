package com.PlayForYouApp.project.controller;

import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.PlayForYouApp.project.entities.Song;
import com.PlayForYouApp.project.service.SongService;
import com.PlayForYouApp.project.service.StorageService;

@RestController
@RequestMapping("/api/v1/media")
public class MediaController {

    private final SongService songService;
    private final StorageService storageService;

    public MediaController(SongService songService, StorageService storageService) {
        this.songService = songService;
        this.storageService = storageService;
    }

    @GetMapping("/images/{songId}")
    public ResponseEntity<Resource> getImage(@PathVariable Long songId) {
        Song song = songService.getSong(songId);
        if (song.getImagePath() == null || song.getImagePath().isBlank()) {
            return ResponseEntity.notFound().build();
        }
        Resource resource = storageService.load(song.getImagePath());
        MediaType mediaType = song.getImageContentType() == null || song.getImageContentType().isBlank()
            ? MediaType.APPLICATION_OCTET_STREAM
            : MediaType.parseMediaType(song.getImageContentType());

        return ResponseEntity.ok()
            .contentType(mediaType)
            .body(resource);
    }
}
