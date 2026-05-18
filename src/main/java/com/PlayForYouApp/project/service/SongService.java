package com.PlayForYouApp.project.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.PlayForYouApp.project.dto.song.GenreCountDto;
import com.PlayForYouApp.project.dto.song.HomeResponseDto;
import com.PlayForYouApp.project.dto.song.LikeToggleResponse;
import com.PlayForYouApp.project.dto.song.SongDetailDto;
import com.PlayForYouApp.project.dto.song.SongDto;
import com.PlayForYouApp.project.entities.Song;
import com.PlayForYouApp.project.entities.SongLike;
import com.PlayForYouApp.project.entities.User;
import com.PlayForYouApp.project.exception.ApiException;
import com.PlayForYouApp.project.repositories.SongLikeRepository;
import com.PlayForYouApp.project.repositories.SongRepository;
import com.PlayForYouApp.project.security.AppUserDetails;

@Service
public class SongService {

    private final SongRepository songRepository;
    private final SongLikeRepository songLikeRepository;
    private final UserService userService;
    private final MusicMapper musicMapper;

    public SongService(
        SongRepository songRepository,
        SongLikeRepository songLikeRepository,
        UserService userService,
        MusicMapper musicMapper
    ) {
        this.songRepository = songRepository;
        this.songLikeRepository = songLikeRepository;
        this.userService = userService;
        this.musicMapper = musicMapper;
    }

    @Transactional(readOnly = true)
    public List<SongDto> getAllSongs(AppUserDetails principal) {
        Set<Long> likedSongIds = likedSongIds(principal);
        List<Song> songs = songRepository.findAll()
            .stream()
            .sorted(Comparator.comparing(Song::getCreatedAt).reversed())
            .toList();
        return musicMapper.toSongDtos(songs, likedSongIds);
    }

    @Transactional(readOnly = true)
    public SongDetailDto getSongDetail(Long songId, AppUserDetails principal) {
        Song song = getSong(songId);
        Set<Long> likedSongIds = likedSongIds(principal);
        List<SongDto> relatedSongs = songRepository.findAll()
            .stream()
            .filter(candidate -> !candidate.getId().equals(song.getId()))
            .filter(candidate -> candidate.getGenre().equalsIgnoreCase(song.getGenre()) || candidate.getArtist().equalsIgnoreCase(song.getArtist()))
            .sorted(Comparator.comparing(Song::getLikeCount).reversed().thenComparing(Song::getCreatedAt).reversed())
            .limit(6)
            .map(candidate -> musicMapper.toSongDto(candidate, likedSongIds))
            .toList();
        return new SongDetailDto(musicMapper.toSongDto(song, likedSongIds), relatedSongs);
    }

    @Transactional(readOnly = true)
    public List<SongDto> searchSongs(String query, AppUserDetails principal) {
        String safeQuery = query == null ? "" : query.trim();
        if (safeQuery.isBlank()) {
            return List.of();
        }
        Set<Long> likedSongIds = likedSongIds(principal);
        return musicMapper.toSongDtos(
            songRepository.findByTitleContainingIgnoreCaseOrArtistContainingIgnoreCaseOrAlbumContainingIgnoreCaseOrGenreContainingIgnoreCase(
                safeQuery,
                safeQuery,
                safeQuery,
                safeQuery
            ),
            likedSongIds
        );
    }

    @Transactional(readOnly = true)
    public HomeResponseDto getHome(AppUserDetails principal) {
        Set<Long> likedSongIds = likedSongIds(principal);
        List<Song> trending = songRepository.findTop12ByOrderByLikeCountDescCreatedAtDesc();
        List<Song> newReleases = songRepository.findTop12ByOrderByCreatedAtDesc();
        Song featured = !trending.isEmpty() ? trending.get(0) : (newReleases.isEmpty() ? null : newReleases.get(0));
        List<Song> recommended = buildRecommendations(principal, trending, newReleases);
        List<GenreCountDto> genres = songRepository.countSongsByGenre()
            .stream()
            .map(row -> new GenreCountDto((String) row[0], (Long) row[1]))
            .limit(6)
            .toList();

        return new HomeResponseDto(
            featured == null ? null : musicMapper.toSongDto(featured, likedSongIds),
            musicMapper.toSongDtos(trending, likedSongIds),
            musicMapper.toSongDtos(newReleases, likedSongIds),
            musicMapper.toSongDtos(recommended, likedSongIds),
            genres
        );
    }

    @Transactional
    public LikeToggleResponse toggleLike(Long songId, AppUserDetails principal) {
        User user = userService.requireUser(principal);
        Song song = getSong(songId);
        return songLikeRepository.findByUserIdAndSongId(user.getId(), songId)
            .map(existingLike -> {
                songLikeRepository.delete(existingLike);
                song.setLikeCount(Math.max(0L, song.getLikeCount() - 1));
                songRepository.save(song);
                return new LikeToggleResponse(false, song.getLikeCount());
            })
            .orElseGet(() -> {
                SongLike songLike = new SongLike();
                songLike.setSong(song);
                songLike.setUser(user);
                songLikeRepository.save(songLike);
                song.setLikeCount(song.getLikeCount() + 1);
                songRepository.save(song);
                return new LikeToggleResponse(true, song.getLikeCount());
            });
    }

    @Transactional
    public void incrementPlayCount(Song song) {
        song.setPlayCount(song.getPlayCount() + 1);
        songRepository.save(song);
    }

    @Transactional(readOnly = true)
    public Song getSong(Long songId) {
        return songRepository.findById(songId)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Song not found"));
    }

    private List<Song> buildRecommendations(AppUserDetails principal, List<Song> trending, List<Song> newReleases) {
        if (principal == null) {
            return trending.stream().limit(8).toList();
        }
        Set<String> preferredGenres = userService.getLikedSongIds(principal.getId())
            .stream()
            .map(this::getSong)
            .map(Song::getGenre)
            .filter(genre -> genre != null && !genre.isBlank())
            .collect(Collectors.toCollection(LinkedHashSet::new));
        if (preferredGenres.isEmpty()) {
            return trending.stream().limit(8).toList();
        }

        List<Song> recommended = songRepository.findAll()
            .stream()
            .filter(song -> preferredGenres.contains(song.getGenre()))
            .sorted(Comparator.comparing(Song::getLikeCount).reversed().thenComparing(Song::getCreatedAt).reversed())
            .limit(8)
            .toList();

        if (!recommended.isEmpty()) {
            return recommended;
        }

        List<Song> fallback = new ArrayList<>(trending);
        fallback.addAll(newReleases);
        return fallback.stream().distinct().limit(8).toList();
    }

    private Set<Long> likedSongIds(AppUserDetails principal) {
        return principal == null ? Set.of() : userService.getLikedSongIds(principal.getId());
    }
}
