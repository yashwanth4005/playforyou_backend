package com.PlayForYouApp.project.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.PlayForYouApp.project.entities.SongLike;

public interface SongLikeRepository extends JpaRepository<SongLike, Long> {

    boolean existsByUserIdAndSongId(Long userId, Long songId);

    Optional<SongLike> findByUserIdAndSongId(Long userId, Long songId);

    List<SongLike> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<SongLike> findTop20ByUserIdOrderByCreatedAtDesc(Long userId);

    long countByUserId(Long userId);

    long countBySongId(Long songId);
}
