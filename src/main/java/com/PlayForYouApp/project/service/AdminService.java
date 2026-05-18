package com.PlayForYouApp.project.service;

import java.util.List;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.PlayForYouApp.project.dto.admin.AdminDashboardDto;
import com.PlayForYouApp.project.dto.admin.TopGenreDto;
import com.PlayForYouApp.project.dto.admin.UpdateSongRequest;
import com.PlayForYouApp.project.dto.song.SongDto;
import com.PlayForYouApp.project.entities.Song;
import com.PlayForYouApp.project.exception.ApiException;
import com.PlayForYouApp.project.repositories.PlaylistRepository;
import com.PlayForYouApp.project.repositories.PlaylistSongRepository;
import com.PlayForYouApp.project.repositories.SongLikeRepository;
import com.PlayForYouApp.project.repositories.SongRepository;
import com.PlayForYouApp.project.repositories.UserRepository;

@Service
public class AdminService {

    private final SongRepository songRepository;
    private final SongLikeRepository songLikeRepository;
    private final PlaylistRepository playlistRepository;
    private final PlaylistSongRepository playlistSongRepository;
    private final UserRepository userRepository;
    private final StorageService storageService;
    private final MusicMapper musicMapper;
    private final SongService songService;

    public AdminService(
        SongRepository songRepository,
        SongLikeRepository songLikeRepository,
        PlaylistRepository playlistRepository,
        PlaylistSongRepository playlistSongRepository,
        UserRepository userRepository,
        StorageService storageService,
        MusicMapper musicMapper,
        SongService songService
    ) {
        this.songRepository = songRepository;
        this.songLikeRepository = songLikeRepository;
        this.playlistRepository = playlistRepository;
        this.playlistSongRepository = playlistSongRepository;
        this.userRepository = userRepository;
        this.storageService = storageService;
        this.musicMapper = musicMapper;
        this.songService = songService;
    }

    @Transactional
    public SongDto uploadSong(
        String title,
        String artist,
        String album,
        String genre,
        Integer duration,
        String description,
        MultipartFile audioFile,
        MultipartFile imageFile
    ) {
        validateSongPayload(title, artist, album, genre, duration);

        StoredFile storedAudio = storageService.storeAudio(audioFile);
        StoredFile storedImage = imageFile == null || imageFile.isEmpty() ? null : storageService.storeImage(imageFile);

        Song song = new Song();
        song.setTitle(title.trim());
        song.setArtist(artist.trim());
        song.setAlbum(album.trim());
        song.setGenre(genre.trim());
        song.setDuration(duration);
        song.setDescription(description == null ? "" : description.trim());
        song.setAudioPath(storedAudio.path());
        song.setAudioContentType(storedAudio.contentType());
        song.setFileUrl("");
        song.setImagePath(storedImage == null ? null : storedImage.path());
        song.setImageContentType(storedImage == null ? null : storedImage.contentType());

        Song savedSong = songRepository.save(song);
        savedSong.setFileUrl("/api/v1/songs/stream/" + savedSong.getId());
        savedSong.setImageUrl(storedImage == null ? null : "/api/v1/media/images/" + savedSong.getId());
        return musicMapper.toSongDto(songRepository.save(savedSong), Set.of());
    }

    @Transactional(readOnly = true)
    public List<SongDto> getAdminSongs() {
        return musicMapper.toSongDtos(
            songRepository.findAll().stream().sorted((left, right) -> right.getCreatedAt().compareTo(left.getCreatedAt())).toList(),
            Set.of()
        );
    }

    @Transactional
    public SongDto updateSong(Long songId, UpdateSongRequest request) {
        Song song = songService.getSong(songId);
        song.setTitle(request.title().trim());
        song.setArtist(request.artist().trim());
        song.setAlbum(request.album().trim());
        song.setGenre(request.genre().trim());
        song.setDuration(request.duration());
        song.setDescription(request.description() == null ? "" : request.description().trim());
        return musicMapper.toSongDto(songRepository.save(song), Set.of());
    }

    @Transactional
    public void deleteSong(Long songId) {
        Song song = songService.getSong(songId);
        playlistSongRepository.deleteAll(playlistSongRepository.findAll().stream()
            .filter(playlistSong -> playlistSong.getSong().getId().equals(songId))
            .toList());
        songLikeRepository.deleteAll(songLikeRepository.findAll().stream()
            .filter(songLike -> songLike.getSong().getId().equals(songId))
            .toList());
        storageService.deleteIfExists(song.getAudioPath());
        storageService.deleteIfExists(song.getImagePath());
        songRepository.delete(song);
    }

    @Transactional(readOnly = true)
    public AdminDashboardDto getDashboard() {
        List<TopGenreDto> topGenres = songRepository.countSongsByGenre()
            .stream()
            .map(row -> new TopGenreDto((String) row[0], (Long) row[1]))
            .limit(5)
            .toList();
        List<SongDto> recentUploads = musicMapper.toSongDtos(songRepository.findTop12ByOrderByCreatedAtDesc(), Set.of());

        return new AdminDashboardDto(
            songRepository.count(),
            userRepository.count(),
            playlistRepository.count(),
            songLikeRepository.count(),
            recentUploads,
            topGenres
        );
    }

    private void validateSongPayload(String title, String artist, String album, String genre, Integer duration) {
        if (title == null || title.isBlank() || artist == null || artist.isBlank() || album == null || album.isBlank() || genre == null || genre.isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Song metadata is incomplete");
        }
        if (duration == null || duration <= 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Duration must be greater than zero");
        }
    }
}
