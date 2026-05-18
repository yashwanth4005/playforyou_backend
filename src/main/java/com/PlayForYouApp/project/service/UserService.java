package com.PlayForYouApp.project.service;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.PlayForYouApp.project.dto.user.LibraryDto;
import com.PlayForYouApp.project.dto.user.ProfileDto;
import com.PlayForYouApp.project.dto.user.ProfileUpdateRequest;
import com.PlayForYouApp.project.dto.user.UserDto;
import com.PlayForYouApp.project.entities.Playlist;
import com.PlayForYouApp.project.entities.PlaylistSong;
import com.PlayForYouApp.project.entities.Song;
import com.PlayForYouApp.project.entities.SongLike;
import com.PlayForYouApp.project.entities.User;
import com.PlayForYouApp.project.enums.Role;
import com.PlayForYouApp.project.exception.ApiException;
import com.PlayForYouApp.project.repositories.PlaylistRepository;
import com.PlayForYouApp.project.repositories.PlaylistSongRepository;
import com.PlayForYouApp.project.repositories.SongLikeRepository;
import com.PlayForYouApp.project.repositories.SongRepository;
import com.PlayForYouApp.project.repositories.UserRepository;
import com.PlayForYouApp.project.security.AppUserDetails;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final SongRepository songRepository;
    private final SongLikeRepository songLikeRepository;
    private final PlaylistRepository playlistRepository;
    private final PlaylistSongRepository playlistSongRepository;
    private final MusicMapper musicMapper;

    public UserService(
        UserRepository userRepository,
        SongRepository songRepository,
        SongLikeRepository songLikeRepository,
        PlaylistRepository playlistRepository,
        PlaylistSongRepository playlistSongRepository,
        MusicMapper musicMapper
    ) {
        this.userRepository = userRepository;
        this.songRepository = songRepository;
        this.songLikeRepository = songLikeRepository;
        this.playlistRepository = playlistRepository;
        this.playlistSongRepository = playlistSongRepository;
        this.musicMapper = musicMapper;
    }

    @Transactional(readOnly = true)
    public User requireUser(AppUserDetails principal) {
        if (principal == null) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }
        return userRepository.findById(principal.getId())
            .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "User not found"));
    }

    @Transactional(readOnly = true)
    public UserDto getMe(AppUserDetails principal) {
        return musicMapper.toUserDto(requireUser(principal));
    }

    @Transactional(readOnly = true)
    public LibraryDto getLibrary(AppUserDetails principal) {
        User user = requireUser(principal);
        Set<Long> likedSongIds = getLikedSongIds(user.getId());
        List<Song> likedSongs = songLikeRepository.findByUserIdOrderByCreatedAtDesc(user.getId())
            .stream()
            .map(SongLike::getSong)
            .toList();
        List<Playlist> playlists = playlistRepository.findByUserIdOrderByUpdatedAtDesc(user.getId());

        return new LibraryDto(
            musicMapper.toSongDtos(likedSongs, likedSongIds),
            playlists.stream().map(playlist -> toPlaylistDto(playlist, likedSongIds)).toList()
        );
    }

    @Transactional(readOnly = true)
    public ProfileDto getProfile(AppUserDetails principal) {
        User user = requireUser(principal);
        List<SongLike> likes = songLikeRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
        Set<String> favoriteGenres = likes.stream()
            .map(songLike -> songLike.getSong().getGenre())
            .filter(genre -> genre != null && !genre.isBlank())
            .collect(Collectors.toCollection(LinkedHashSet::new));

        long uploadedSongsCount = user.getRole() == Role.ROLE_ADMIN ? songRepository.count() : 0L;
        return new ProfileDto(
            musicMapper.toUserDto(user),
            playlistRepository.countByUserId(user.getId()),
            songLikeRepository.countByUserId(user.getId()),
            uploadedSongsCount,
            favoriteGenres.stream().limit(5).toList()
        );
    }

    @Transactional
    public UserDto updateProfile(AppUserDetails principal, ProfileUpdateRequest request) {
        User user = requireUser(principal);
        String normalizedEmail = request.email().trim().toLowerCase();
        if (!normalizedEmail.equals(user.getEmail()) && userRepository.existsByEmail(normalizedEmail)) {
            throw new ApiException(HttpStatus.CONFLICT, "Email already registered");
        }
        user.setName(request.name().trim());
        user.setEmail(normalizedEmail);
        return musicMapper.toUserDto(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public Set<Long> getLikedSongIds(Long userId) {
        return songLikeRepository.findByUserIdOrderByCreatedAtDesc(userId)
            .stream()
            .map(songLike -> songLike.getSong().getId())
            .collect(Collectors.toSet());
    }

    private com.PlayForYouApp.project.dto.playlist.PlaylistDto toPlaylistDto(Playlist playlist, Set<Long> likedSongIds) {
        List<com.PlayForYouApp.project.dto.song.SongDto> songs = playlistSongRepository.findByPlaylistIdOrderByPositionAsc(playlist.getId())
            .stream()
            .map(PlaylistSong::getSong)
            .map(song -> musicMapper.toSongDto(song, likedSongIds))
            .toList();
        return musicMapper.toPlaylistDto(playlist, songs);
    }
}
