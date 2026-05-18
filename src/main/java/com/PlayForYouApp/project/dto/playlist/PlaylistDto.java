package com.PlayForYouApp.project.dto.playlist;

import java.time.LocalDateTime;
import java.util.List;

import com.PlayForYouApp.project.dto.song.SongDto;

public record PlaylistDto(
    Long id,
    Long userId,
    String name,
    String description,
    String coverImageUrl,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    int songCount,
    List<SongDto> songs
) {
}
