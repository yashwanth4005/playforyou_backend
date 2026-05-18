package com.PlayForYouApp.project.dto.player;

import java.util.List;

import com.PlayForYouApp.project.dto.song.SongDto;

public class PlayerStateDto {

    private SongDto currentSong;
    private boolean playing;
    private double progress;
    private double duration;
    private List<SongDto> queue;

    public PlayerStateDto() {}

    public PlayerStateDto(SongDto currentSong, boolean playing, double progress, double duration, List<SongDto> queue) {
        this.currentSong = currentSong;
        this.playing = playing;
        this.progress = progress;
        this.duration = duration;
        this.queue = queue;
    }

    public SongDto getCurrentSong() {
        return currentSong;
    }

    public void setCurrentSong(SongDto currentSong) {
        this.currentSong = currentSong;
    }

    public boolean isPlaying() {
        return playing;
    }

    public void setPlaying(boolean playing) {
        this.playing = playing;
    }

    public double getProgress() {
        return progress;
    }

    public void setProgress(double progress) {
        this.progress = progress;
    }

    public double getDuration() {
        return duration;
    }

    public void setDuration(double duration) {
        this.duration = duration;
    }

    public List<SongDto> getQueue() {
        return queue;
    }

    public void setQueue(List<SongDto> queue) {
        this.queue = queue;
    }
}
