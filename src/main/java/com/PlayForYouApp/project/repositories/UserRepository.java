package com.PlayForYouApp.project.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.PlayForYouApp.project.entities.User;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
}
