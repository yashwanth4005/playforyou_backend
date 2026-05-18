package com.PlayForYouApp.project.dto.admin;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateSongRequest(
    @NotBlank @Size(min = 2, max = 160) String title,
    @NotBlank @Size(min = 2, max = 160) String artist,
    @NotBlank @Size(min = 2, max = 160) String album,
    @NotBlank @Size(min = 2, max = 80) String genre,
    @NotNull @Min(1) Integer duration,
    @Size(max = 1000) String description
) {
}
