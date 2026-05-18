package com.PlayForYouApp.project.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.PlayForYouApp.project.dto.player.PlayerStateDto;
import com.PlayForYouApp.project.dto.song.SongDto;
import com.PlayForYouApp.project.entities.Song;
import com.PlayForYouApp.project.security.AppUserDetails;

@Service
public class PlayerService {

    private static class PlayerState {
        private Song currentSong;
        private boolean playing;
        private double progress;
        private List<Song> queue = new ArrayList<>();
    }

    private final Map<Long, PlayerState> stateByUser = new ConcurrentHashMap<>();
    private final SongService songService;
    private final MusicMapper musicMapper;

    public PlayerService(SongService songService, MusicMapper musicMapper) {
        this.songService = songService;
        this.musicMapper = musicMapper;
    }

    private PlayerState stateFor(AppUserDetails principal) {
        if (principal == null) {
            throw new IllegalArgumentException("User not authenticated");
        }
        return stateByUser.computeIfAbsent(principal.getId(), key -> new PlayerState());
    }

    public PlayerStateDto getPlayerState(AppUserDetails principal) {
        PlayerState state = stateFor(principal);
        SongDto currentSongDto = state.currentSong == null ? null : musicMapper.toSongDto(state.currentSong, null);
        List<SongDto> queueDtos = new ArrayList<>();
        for (Song song : state.queue) {
            queueDtos.add(musicMapper.toSongDto(song, null));
        }
        return new PlayerStateDto(currentSongDto, state.playing, state.progress, state.currentSong == null ? 0 : state.currentSong.getDuration(), queueDtos);
    }

    public PlayerStateDto loadSong(AppUserDetails principal, Long songId, List<Long> queueSongIds) {
        PlayerState state = stateFor(principal);
        Song song = songService.getSong(songId);
        state.currentSong = song;
        state.playing = true;
        state.progress = 0;
        state.queue = new ArrayList<>();

        if (queueSongIds != null) {
            for (Long id : queueSongIds) {
                try {
                    state.queue.add(songService.getSong(id));
                } catch (Exception ignored) {
                }
            }
        }

        return getPlayerState(principal);
    }

    public PlayerStateDto play(AppUserDetails principal) {
        PlayerState state = stateFor(principal);
        if (state.currentSong == null) {
            return getPlayerState(principal);
        }
        state.playing = true;
        return getPlayerState(principal);
    }

    public PlayerStateDto pause(AppUserDetails principal) {
        PlayerState state = stateFor(principal);
        state.playing = false;
        return getPlayerState(principal);
    }

    public PlayerStateDto seek(AppUserDetails principal, double timeSeconds) {
        PlayerState state = stateFor(principal);
        state.progress = Math.max(0, Math.min(timeSeconds, state.currentSong == null ? 0 : state.currentSong.getDuration()));
        return getPlayerState(principal);
    }

    public PlayerStateDto next(AppUserDetails principal) {
        PlayerState state = stateFor(principal);
        if (state.queue.isEmpty()) {
            state.playing = false;
            return getPlayerState(principal);
        }

        int idx = findCurrentIndex(state);
        if (idx < 0 || idx + 1 >= state.queue.size()) {
            state.playing = false;
            return getPlayerState(principal);
        }

        state.currentSong = state.queue.get(idx + 1);
        state.progress = 0;
        state.playing = true;
        return getPlayerState(principal);
    }

    public PlayerStateDto previous(AppUserDetails principal) {
        PlayerState state = stateFor(principal);
        if (state.queue.isEmpty()) {
            return getPlayerState(principal);
        }

        int idx = findCurrentIndex(state);
        if (idx <= 0) {
            state.progress = 0;
            return getPlayerState(principal);
        }

        state.currentSong = state.queue.get(idx - 1);
        state.progress = 0;
        state.playing = true;
        return getPlayerState(principal);
    }

    private int findCurrentIndex(PlayerState state) {
        if (state.currentSong == null) {
            return -1;
        }
        for (int index = 0; index < state.queue.size(); index++) {
            if (state.queue.get(index).getId().equals(state.currentSong.getId())) {
                return index;
            }
        }
        return -1;
    }
}
