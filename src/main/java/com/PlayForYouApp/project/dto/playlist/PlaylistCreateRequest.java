package com.PlayForYouApp.project.dto.playlist;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PlaylistCreateRequest(
    @NotBlank @Size(min = 2, max = 120) String name,
    @Size(max = 400) String description,
    List<Long> songIds
) {
}
