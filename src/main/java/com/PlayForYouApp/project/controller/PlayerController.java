package com.PlayForYouApp.project.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.PlayForYouApp.project.dto.player.PlayerStateDto;
import com.PlayForYouApp.project.security.AppUserDetails;
import com.PlayForYouApp.project.service.PlayerService;

@RestController
@RequestMapping("/api/v1/player")
public class PlayerController {

    private final PlayerService playerService;

    public PlayerController(PlayerService playerService) {
        this.playerService = playerService;
    }

    @GetMapping("/state")
    public PlayerStateDto getPlayerState(@AuthenticationPrincipal AppUserDetails principal) {
        return playerService.getPlayerState(principal);
    }

    @PostMapping("/load/{songId}")
    public PlayerStateDto loadSong(
        @AuthenticationPrincipal AppUserDetails principal,
        @PathVariable Long songId,
        @RequestParam(value = "queue", required = false) String queueIds
    ) {
        var queue = queueIds == null || queueIds.isBlank() ? null :
            java.util.Arrays.stream(queueIds.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .map(Long::valueOf)
                .toList();
        return playerService.loadSong(principal, songId, queue);
    }

    @PostMapping("/play")
    public PlayerStateDto play(@AuthenticationPrincipal AppUserDetails principal) {
        return playerService.play(principal);
    }

    @PostMapping("/pause")
    public PlayerStateDto pause(@AuthenticationPrincipal AppUserDetails principal) {
        return playerService.pause(principal);
    }

    @PostMapping("/next")
    public PlayerStateDto next(@AuthenticationPrincipal AppUserDetails principal) {
        return playerService.next(principal);
    }

    @PostMapping("/previous")
    public PlayerStateDto previous(@AuthenticationPrincipal AppUserDetails principal) {
        return playerService.previous(principal);
    }

    @PostMapping("/seek")
    public PlayerStateDto seek(@AuthenticationPrincipal AppUserDetails principal, @RequestParam double time) {
        return playerService.seek(principal, time);
    }
}
