package com.PlayForYouApp.project.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.PlayForYouApp.project.entities.User;
import com.PlayForYouApp.project.enums.Role;
import com.PlayForYouApp.project.repositories.UserRepository;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.existsByEmail("admin@playforyou.com")) {
            return;
        }

        User admin = new User();
        admin.setName("PlayForYou Admin");
        admin.setEmail("admin@playforyou.com");
        admin.setPassword(passwordEncoder.encode("Admin@123"));
        admin.setRole(Role.ROLE_ADMIN);
        userRepository.save(admin);
    }
}
