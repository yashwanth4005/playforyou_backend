package com.PlayForYouApp.project.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourceRegion;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRange;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.PlayForYouApp.project.dto.song.HomeResponseDto;
import com.PlayForYouApp.project.dto.song.LikeToggleResponse;
import com.PlayForYouApp.project.dto.song.SongDetailDto;
import com.PlayForYouApp.project.dto.song.SongDto;
import com.PlayForYouApp.project.entities.Song;
import com.PlayForYouApp.project.security.AppUserDetails;
import com.PlayForYouApp.project.service.SongService;
import com.PlayForYouApp.project.service.StorageService;

@RestController
@RequestMapping("/api/v1/songs")
public class SongController {

    private static final long CHUNK_SIZE = 1024 * 1024;

    private final SongService songService;
    private final StorageService storageService;

    public SongController(SongService songService, StorageService storageService) {
        this.songService = songService;
        this.storageService = storageService;
    }

    @GetMapping
    public List<SongDto> getSongs(@AuthenticationPrincipal AppUserDetails principal) {
        return songService.getAllSongs(principal);
    }

    @GetMapping("/home")
    public HomeResponseDto getHome(@AuthenticationPrincipal AppUserDetails principal) {
        return songService.getHome(principal);
    }

    @GetMapping("/search")
    public List<SongDto> searchSongs(
        @RequestParam(name = "q", required = false) String query,
        @AuthenticationPrincipal AppUserDetails principal
    ) {
        return songService.searchSongs(query, principal);
    }

    @GetMapping("/{songId}")
    public SongDetailDto getSong(@PathVariable Long songId, @AuthenticationPrincipal AppUserDetails principal) {
        return songService.getSongDetail(songId, principal);
    }

    @PostMapping("/{songId}/like")
    public LikeToggleResponse toggleLike(
        @PathVariable Long songId,
        @AuthenticationPrincipal AppUserDetails principal
    ) {
        return songService.toggleLike(songId, principal);
    }

    @GetMapping("/stream/{songId}")
    public ResponseEntity<ResourceRegion> streamSong(
        @PathVariable Long songId,
        @RequestHeader HttpHeaders headers
    ) throws IOException {
        Song song = songService.getSong(songId);
        Resource resource = storageService.load(song.getAudioPath());
        long contentLength = resource.contentLength();

        ResourceRegion region;
        HttpStatus status;
        long regionStart = 0;

        if (headers.getRange().isEmpty()) {
            region = new ResourceRegion(resource, 0, Math.min(CHUNK_SIZE, contentLength));
            status = HttpStatus.OK;
        } else {
            HttpRange range = headers.getRange().get(0);
            regionStart = range.getRangeStart(contentLength);
            long end = range.getRangeEnd(contentLength);
            long rangeLength = Math.min(CHUNK_SIZE, end - regionStart + 1);
            region = new ResourceRegion(resource, regionStart, rangeLength);
            status = HttpStatus.PARTIAL_CONTENT;
        }

        if (regionStart == 0) {
            songService.incrementPlayCount(song);
        }

        MediaType mediaType = resolveMediaType(song.getAudioContentType());
        return ResponseEntity.status(status)
            .contentType(mediaType)
            .header(HttpHeaders.ACCEPT_RANGES, "bytes")
            .contentLength(region.getCount())
            .body(region);
    }

    private MediaType resolveMediaType(String value) {
        try {
            return value == null || value.isBlank() ? MediaType.APPLICATION_OCTET_STREAM : MediaType.parseMediaType(value);
        } catch (HttpMessageNotWritableException exception) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
    }
}
