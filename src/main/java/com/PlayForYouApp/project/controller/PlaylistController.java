package com.PlayForYouApp.project.controller;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.PlayForYouApp.project.dto.playlist.PlaylistCreateRequest;
import com.PlayForYouApp.project.dto.playlist.PlaylistDto;
import com.PlayForYouApp.project.security.AppUserDetails;
import com.PlayForYouApp.project.service.PlaylistService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/playlists")
public class PlaylistController {

    private final PlaylistService playlistService;

    public PlaylistController(PlaylistService playlistService) {
        this.playlistService = playlistService;
    }

    @PostMapping
    public PlaylistDto createPlaylist(
        @Valid @RequestBody PlaylistCreateRequest request,
        @AuthenticationPrincipal AppUserDetails principal
    ) {
        return playlistService.createPlaylist(request, principal);
    }

    @GetMapping("/{userId}")
    public List<PlaylistDto> getPlaylists(
        @PathVariable Long userId,
        @AuthenticationPrincipal AppUserDetails principal
    ) {
        return playlistService.getPlaylists(userId, principal);
    }

    @PutMapping("/{playlistId}")
    public PlaylistDto updatePlaylist(
        @PathVariable Long playlistId,
        @Valid @RequestBody PlaylistCreateRequest request,
        @AuthenticationPrincipal AppUserDetails principal
    ) {
        return playlistService.updatePlaylist(playlistId, request, principal);
    }

    @PostMapping("/{playlistId}/songs/{songId}")
    public PlaylistDto addSong(
        @PathVariable Long playlistId,
        @PathVariable Long songId,
        @AuthenticationPrincipal AppUserDetails principal
    ) {
        return playlistService.addSong(playlistId, songId, principal);
    }

    @DeleteMapping("/{playlistId}/songs/{songId}")
    public PlaylistDto removeSong(
        @PathVariable Long playlistId,
        @PathVariable Long songId,
        @AuthenticationPrincipal AppUserDetails principal
    ) {
        return playlistService.removeSong(playlistId, songId, principal);
    }
}
