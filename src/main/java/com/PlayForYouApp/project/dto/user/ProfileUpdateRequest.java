package com.PlayForYouApp.project.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProfileUpdateRequest(
    @NotBlank @Size(min = 2, max = 120) String name,
    @Email @NotBlank String email
) {
}
