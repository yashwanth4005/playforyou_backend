package com.PlayForYouApp.project.dto.auth;

import com.PlayForYouApp.project.dto.user.UserDto;

public record AuthResponse(
    String token,
    UserDto user
) {
}
