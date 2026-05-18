package com.PlayForYouApp.project.dto.user;

import java.time.LocalDateTime;

public record UserDto(
    Long id,
    String name,
    String email,
    String role,
    LocalDateTime createdAt
) {
}
