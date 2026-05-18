package com.PlayForYouApp.project.dto.user;

import java.util.List;

public record ProfileDto(
    UserDto user,
    long playlistsCount,
    long likedSongsCount,
    long uploadedSongsCount,
    List<String> favoriteGenres
) {
}
