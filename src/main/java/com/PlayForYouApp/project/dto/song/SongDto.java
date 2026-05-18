package com.PlayForYouApp.project.dto.song;

import java.time.LocalDateTime;

public record SongDto(
    Long id,
    String title,
    String artist,
    String album,
    String genre,
    Integer duration,
    String description,
    String fileUrl,
    String imageUrl,
    String streamUrl,
    LocalDateTime createdAt,
    Long playCount,
    Long likeCount,
    boolean liked
) {
}
