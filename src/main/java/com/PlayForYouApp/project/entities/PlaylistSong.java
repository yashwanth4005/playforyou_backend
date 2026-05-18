package com.PlayForYouApp.project.entities;

import java.time.LocalDateTime;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "playlist_songs")
public class PlaylistSong {

    @EmbeddedId
    private PlaylistSongId id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("playlistId")
    @JoinColumn(name = "playlist_id", nullable = false)
    private Playlist playlist;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("songId")
    @JoinColumn(name = "song_id", nullable = false)
    private Song song;

    private Integer position;

    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public PlaylistSongId getId() {
        return id;
    }

    public void setId(PlaylistSongId id) {
        this.id = id;
    }

    public Playlist getPlaylist() {
        return playlist;
    }

    public void setPlaylist(Playlist playlist) {
        this.playlist = playlist;
    }

    public Song getSong() {
        return song;
    }

    public void setSong(Song song) {
        this.song = song;
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
