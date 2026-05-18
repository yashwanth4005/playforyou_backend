package com.PlayForYouApp.project.dto.admin;

import java.util.List;

import com.PlayForYouApp.project.dto.song.SongDto;

public record AdminDashboardDto(
    long totalSongs,
    long totalUsers,
    long totalPlaylists,
    long totalLikes,
    List<SongDto> recentUploads,
    List<TopGenreDto> topGenres
) {
}
