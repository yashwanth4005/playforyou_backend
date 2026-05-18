package com.PlayForYouApp.project.dto.user;

import java.util.List;

import com.PlayForYouApp.project.dto.playlist.PlaylistDto;
import com.PlayForYouApp.project.dto.song.SongDto;

public record LibraryDto(
    List<SongDto> likedSongs,
    List<PlaylistDto> playlists
) {
}
