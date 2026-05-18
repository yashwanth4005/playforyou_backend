package com.PlayForYouApp.project.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.PlayForYouApp.project.exception.ApiException;

import jakarta.annotation.PostConstruct;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@Service
public class StorageService {

    private final Path rootPath;

    public StorageService(@Value("${app.storage.root}") String rootDirectory) {
        this.rootPath = Paths.get(rootDirectory).toAbsolutePath().normalize();
    }

    @PostConstruct
    void init() {
        try {
            Files.createDirectories(rootPath.resolve("audio"));
            Files.createDirectories(rootPath.resolve("images"));
        } catch (IOException exception) {
            throw new ApiException(INTERNAL_SERVER_ERROR, "Unable to initialize file storage");
        }
    }

    public StoredFile storeAudio(MultipartFile file) {
        return store(file, "audio");
    }

    public StoredFile storeImage(MultipartFile file) {
        return store(file, "images");
    }

    public Resource load(String relativePath) {
        try {
            Path path = rootPath.resolve(relativePath).normalize();
            Resource resource = new PathResource(path);
            if (!resource.exists()) {
                throw new ApiException(BAD_REQUEST, "File not found");
            }
            return resource;
        } catch (Exception exception) {
            if (exception instanceof ApiException apiException) {
                throw apiException;
            }
            throw new ApiException(INTERNAL_SERVER_ERROR, "Unable to load file");
        }
    }

    public void deleteIfExists(String relativePath) {
        if (relativePath == null || relativePath.isBlank()) {
            return;
        }
        try {
            Files.deleteIfExists(rootPath.resolve(relativePath).normalize());
        } catch (IOException exception) {
            throw new ApiException(INTERNAL_SERVER_ERROR, "Unable to delete file");
        }
    }

    private StoredFile store(MultipartFile file, String folder) {
        if (file == null || file.isEmpty()) {
            throw new ApiException(BAD_REQUEST, "Required file is missing");
        }
        String originalName = Objects.requireNonNullElse(file.getOriginalFilename(), "file.bin");
        String extension = "";
        int extensionIndex = originalName.lastIndexOf('.');
        if (extensionIndex >= 0) {
            extension = originalName.substring(extensionIndex);
        }
        String filename = UUID.randomUUID() + extension;
        Path destination = rootPath.resolve(folder).resolve(filename).normalize();
        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, destination, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException exception) {
            throw new ApiException(INTERNAL_SERVER_ERROR, "Unable to store uploaded file");
        }
        return new StoredFile(folder + "/" + filename, file.getContentType());
    }
}
