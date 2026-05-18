package com.PlayForYouApp.project.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.PlayForYouApp.project.dto.user.LibraryDto;
import com.PlayForYouApp.project.dto.user.ProfileDto;
import com.PlayForYouApp.project.dto.user.ProfileUpdateRequest;
import com.PlayForYouApp.project.dto.user.UserDto;
import com.PlayForYouApp.project.security.AppUserDetails;
import com.PlayForYouApp.project.service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public UserDto getMe(@AuthenticationPrincipal AppUserDetails principal) {
        return userService.getMe(principal);
    }

    @GetMapping("/me/library")
    public LibraryDto getLibrary(@AuthenticationPrincipal AppUserDetails principal) {
        return userService.getLibrary(principal);
    }

    @GetMapping("/me/profile")
    public ProfileDto getProfile(@AuthenticationPrincipal AppUserDetails principal) {
        return userService.getProfile(principal);
    }

    @PutMapping("/me")
    public UserDto updateProfile(
        @AuthenticationPrincipal AppUserDetails principal,
        @Valid @RequestBody ProfileUpdateRequest request
    ) {
        return userService.updateProfile(principal, request);
    }
}
