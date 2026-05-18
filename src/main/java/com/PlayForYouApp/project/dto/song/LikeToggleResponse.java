package com.PlayForYouApp.project.dto.song;

public record LikeToggleResponse(
    boolean liked,
    long likeCount
) {
}
