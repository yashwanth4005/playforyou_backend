package com.PlayForYouApp.project.service;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.PlayForYouApp.project.dto.playlist.PlaylistDto;
import com.PlayForYouApp.project.dto.song.SongDto;
import com.PlayForYouApp.project.dto.user.UserDto;
import com.PlayForYouApp.project.entities.Playlist;
import com.PlayForYouApp.project.entities.Song;
import com.PlayForYouApp.project.entities.User;

@Component
public class MusicMapper {

    public UserDto toUserDto(User user) {
        return new UserDto(
            user.getId(),
            user.getName(),
            user.getEmail(),
            user.getRole().name().replace("ROLE_", ""),
            user.getCreatedAt()
        );
    }

    public SongDto toSongDto(Song song, Set<Long> likedSongIds) {
        boolean liked = likedSongIds != null && likedSongIds.contains(song.getId());
        return new SongDto(
            song.getId(),
            song.getTitle(),
            song.getArtist(),
            song.getAlbum(),
            song.getGenre(),
            song.getDuration(),
            song.getDescription(),
            song.getFileUrl(),
            song.getImageUrl(),
            "/api/v1/songs/stream/" + song.getId(),
            song.getCreatedAt(),
            song.getPlayCount(),
            song.getLikeCount(),
            liked
        );
    }

    public List<SongDto> toSongDtos(List<Song> songs, Set<Long> likedSongIds) {
        if (songs == null || songs.isEmpty()) {
            return Collections.emptyList();
        }
        return songs.stream().map(song -> toSongDto(song, likedSongIds)).toList();
    }

    public PlaylistDto toPlaylistDto(Playlist playlist, List<SongDto> songs) {
        String coverImage = playlist.getCoverImageUrl();
        if ((coverImage == null || coverImage.isBlank()) && songs != null && !songs.isEmpty()) {
            coverImage = songs.get(0).imageUrl();
        }
        return new PlaylistDto(
            playlist.getId(),
            playlist.getUser().getId(),
            playlist.getName(),
            playlist.getDescription(),
            coverImage,
            playlist.getCreatedAt(),
            playlist.getUpdatedAt(),
            songs == null ? 0 : songs.size(),
            songs == null ? List.of() : songs
        );
    }
}
