package com.PlayForYouApp.project.entities;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class PlaylistSongId implements Serializable {

    @Column(name = "playlist_id")
    private Long playlistId;

    @Column(name = "song_id")
    private Long songId;

    public PlaylistSongId() {
    }

    public PlaylistSongId(Long playlistId, Long songId) {
        this.playlistId = playlistId;
        this.songId = songId;
    }

    public Long getPlaylistId() {
        return playlistId;
    }

    public Long getSongId() {
        return songId;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof PlaylistSongId that)) {
            return false;
        }
        return Objects.equals(playlistId, that.playlistId) && Objects.equals(songId, that.songId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(playlistId, songId);
    }
}
