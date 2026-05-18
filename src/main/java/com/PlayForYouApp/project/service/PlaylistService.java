package com.PlayForYouApp.project.service;

import java.util.List;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.PlayForYouApp.project.dto.playlist.PlaylistCreateRequest;
import com.PlayForYouApp.project.dto.playlist.PlaylistDto;
import com.PlayForYouApp.project.dto.song.SongDto;
import com.PlayForYouApp.project.entities.Playlist;
import com.PlayForYouApp.project.entities.PlaylistSong;
import com.PlayForYouApp.project.entities.PlaylistSongId;
import com.PlayForYouApp.project.entities.Song;
import com.PlayForYouApp.project.entities.User;
import com.PlayForYouApp.project.enums.Role;
import com.PlayForYouApp.project.exception.ApiException;
import com.PlayForYouApp.project.repositories.PlaylistRepository;
import com.PlayForYouApp.project.repositories.PlaylistSongRepository;
import com.PlayForYouApp.project.security.AppUserDetails;

@Service
public class PlaylistService {

    private final PlaylistRepository playlistRepository;
    private final PlaylistSongRepository playlistSongRepository;
    private final SongService songService;
    private final UserService userService;
    private final MusicMapper musicMapper;

    public PlaylistService(
        PlaylistRepository playlistRepository,
        PlaylistSongRepository playlistSongRepository,
        SongService songService,
        UserService userService,
        MusicMapper musicMapper
    ) {
        this.playlistRepository = playlistRepository;
        this.playlistSongRepository = playlistSongRepository;
        this.songService = songService;
        this.userService = userService;
        this.musicMapper = musicMapper;
    }

    @Transactional
    public PlaylistDto createPlaylist(PlaylistCreateRequest request, AppUserDetails principal) {
        User user = userService.requireUser(principal);
        Playlist playlist = new Playlist();
        playlist.setUser(user);
        playlist.setName(request.name().trim());
        playlist.setDescription(request.description());
        Playlist savedPlaylist = playlistRepository.save(playlist);

        if (request.songIds() != null) {
            int position = 1;
            for (Long songId : request.songIds().stream().distinct().toList()) {
                Song song = songService.getSong(songId);
                attachSong(savedPlaylist, song, position++);
            }
            updateCover(savedPlaylist);
        }

        return toPlaylistDto(savedPlaylist, userService.getLikedSongIds(user.getId()));
    }

    @Transactional(readOnly = true)
    public List<PlaylistDto> getPlaylists(Long userId, AppUserDetails principal) {
        User currentUser = userService.requireUser(principal);
        if (!currentUser.getId().equals(userId) && currentUser.getRole() != Role.ROLE_ADMIN) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You do not have access to this library");
        }

        Set<Long> likedSongIds = userService.getLikedSongIds(currentUser.getId());
        return playlistRepository.findByUserIdOrderByUpdatedAtDesc(userId)
            .stream()
            .map(playlist -> toPlaylistDto(playlist, likedSongIds))
            .toList();
    }

    @Transactional
    public PlaylistDto updatePlaylist(Long playlistId, PlaylistCreateRequest request, AppUserDetails principal) {
        Playlist playlist = requireOwnedPlaylist(playlistId, principal);
        playlist.setName(request.name().trim());
        playlist.setDescription(request.description());
        Playlist updatedPlaylist = playlistRepository.save(playlist);
        return toPlaylistDto(updatedPlaylist, userService.getLikedSongIds(playlist.getUser().getId()));
    }

    @Transactional
    public PlaylistDto addSong(Long playlistId, Long songId, AppUserDetails principal) {
        Playlist playlist = requireOwnedPlaylist(playlistId, principal);
        if (playlistSongRepository.existsByPlaylistIdAndSongId(playlistId, songId)) {
            throw new ApiException(HttpStatus.CONFLICT, "Song already exists in playlist");
        }
        Song song = songService.getSong(songId);
        int nextPosition = playlistSongRepository.findTopByPlaylistIdOrderByPositionDesc(playlistId)
            .map(playlistSong -> playlistSong.getPosition() + 1)
            .orElse(1);
        attachSong(playlist, song, nextPosition);
        updateCover(playlist);
        return toPlaylistDto(playlist, userService.getLikedSongIds(playlist.getUser().getId()));
    }

    @Transactional
    public PlaylistDto removeSong(Long playlistId, Long songId, AppUserDetails principal) {
        Playlist playlist = requireOwnedPlaylist(playlistId, principal);
        playlistSongRepository.deleteByPlaylistIdAndSongId(playlistId, songId);
        updateCover(playlist);
        return toPlaylistDto(playlist, userService.getLikedSongIds(playlist.getUser().getId()));
    }

    @Transactional(readOnly = true)
    public PlaylistDto toPlaylistDto(Playlist playlist, Set<Long> likedSongIds) {
        List<SongDto> songs = playlistSongRepository.findByPlaylistIdOrderByPositionAsc(playlist.getId())
            .stream()
            .map(PlaylistSong::getSong)
            .map(song -> musicMapper.toSongDto(song, likedSongIds))
            .toList();
        return musicMapper.toPlaylistDto(playlist, songs);
    }

    private void attachSong(Playlist playlist, Song song, int position) {
        PlaylistSong playlistSong = new PlaylistSong();
        playlistSong.setId(new PlaylistSongId(playlist.getId(), song.getId()));
        playlistSong.setPlaylist(playlist);
        playlistSong.setSong(song);
        playlistSong.setPosition(position);
        playlistSongRepository.save(playlistSong);
    }

    private Playlist requireOwnedPlaylist(Long playlistId, AppUserDetails principal) {
        User currentUser = userService.requireUser(principal);
        Playlist playlist = playlistRepository.findById(playlistId)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Playlist not found"));
        if (!playlist.getUser().getId().equals(currentUser.getId()) && currentUser.getRole() != Role.ROLE_ADMIN) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You do not have permission to modify this playlist");
        }
        return playlist;
    }

    private void updateCover(Playlist playlist) {
        String coverImageUrl = playlistSongRepository.findByPlaylistIdOrderByPositionAsc(playlist.getId())
            .stream()
            .findFirst()
            .map(playlistSong -> playlistSong.getSong().getImageUrl())
            .orElse(null);
        playlist.setCoverImageUrl(coverImageUrl);
        playlistRepository.save(playlist);
    }
}
