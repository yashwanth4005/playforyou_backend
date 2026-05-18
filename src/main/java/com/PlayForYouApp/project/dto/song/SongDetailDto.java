package com.PlayForYouApp.project.dto.song;

import java.util.List;

public record SongDetailDto(
    SongDto song,
    List<SongDto> relatedSongs
) {
}
