package com.PlayForYouApp.project.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.PlayForYouApp.project.entities.Playlist;

public interface PlaylistRepository extends JpaRepository<Playlist, Long> {

    List<Playlist> findByUserIdOrderByUpdatedAtDesc(Long userId);

    long countByUserId(Long userId);
}
