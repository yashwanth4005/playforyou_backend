package com.PlayForYouApp.project.dto.song;

import java.util.List;

public record HomeResponseDto(
    SongDto featuredSong,
    List<SongDto> trendingSongs,
    List<SongDto> newReleases,
    List<SongDto> recommendedSongs,
    List<GenreCountDto> genres
) {
}
