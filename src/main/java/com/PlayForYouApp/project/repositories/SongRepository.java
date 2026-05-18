package com.PlayForYouApp.project.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.PlayForYouApp.project.entities.Song;

public interface SongRepository extends JpaRepository<Song, Long> {

    List<Song> findTop12ByOrderByCreatedAtDesc();

    List<Song> findTop12ByOrderByLikeCountDescCreatedAtDesc();

    List<Song> findByTitleContainingIgnoreCaseOrArtistContainingIgnoreCaseOrAlbumContainingIgnoreCaseOrGenreContainingIgnoreCase(
        String title,
        String artist,
        String album,
        String genre
    );

    @Query("select s.genre, count(s) from Song s group by s.genre order by count(s) desc")
    List<Object[]> countSongsByGenre();
}
