package com.PlayForYouApp.project.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.PlayForYouApp.project.dto.admin.AdminDashboardDto;
import com.PlayForYouApp.project.dto.admin.MessageResponse;
import com.PlayForYouApp.project.dto.admin.UpdateSongRequest;
import com.PlayForYouApp.project.dto.song.SongDto;
import com.PlayForYouApp.project.service.AdminService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/dashboard")
    public AdminDashboardDto getDashboard() {
        return adminService.getDashboard();
    }

    @PostMapping("/upload")
    public SongDto uploadSong(
        @RequestParam String title,
        @RequestParam String artist,
        @RequestParam String album,
        @RequestParam String genre,
        @RequestParam Integer duration,
        @RequestParam(required = false) String description,
        @RequestParam("audioFile") MultipartFile audioFile,
        @RequestParam(name = "imageFile", required = false) MultipartFile imageFile
    ) {
        return adminService.uploadSong(title, artist, album, genre, duration, description, audioFile, imageFile);
    }

    @GetMapping("/songs")
    public List<SongDto> getSongs() {
        return adminService.getAdminSongs();
    }

    @PutMapping("/update/{songId}")
    public SongDto updateSong(@PathVariable Long songId, @Valid @org.springframework.web.bind.annotation.RequestBody UpdateSongRequest request) {
        return adminService.updateSong(songId, request);
    }

    @DeleteMapping("/delete/{songId}")
    public MessageResponse deleteSong(@PathVariable Long songId) {
        adminService.deleteSong(songId);
        return new MessageResponse("Song deleted successfully");
    }
}
