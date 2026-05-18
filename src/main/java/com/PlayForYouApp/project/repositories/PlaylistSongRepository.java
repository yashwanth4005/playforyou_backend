package com.PlayForYouApp.project.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.PlayForYouApp.project.entities.PlaylistSong;
import com.PlayForYouApp.project.entities.PlaylistSongId;

public interface PlaylistSongRepository extends JpaRepository<PlaylistSong, PlaylistSongId> {

    List<PlaylistSong> findByPlaylistIdOrderByPositionAsc(Long playlistId);

    boolean existsByPlaylistIdAndSongId(Long playlistId, Long songId);

    long countByPlaylistId(Long playlistId);

    void deleteByPlaylistIdAndSongId(Long playlistId, Long songId);

    Optional<PlaylistSong> findTopByPlaylistIdOrderByPositionDesc(Long playlistId);
}
