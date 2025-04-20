package com.ist.leave_ms.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;

@Service
public class FileStorageService {

    @Value("${file.storage.profile-pictures-dir:/uploads/profile-pictures}")
    private String profilePicturesDir;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    public String storeProfilePicture(InputStream inputStream, String fileName) throws IOException {
        // Ensure directory exists
        Path directoryPath = Paths.get(profilePicturesDir);
        if (!Files.exists(directoryPath)) {
            Files.createDirectories(directoryPath);
        }

        // Store file
        Path filePath = directoryPath.resolve(fileName);
        Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);

        // Return URL
        return baseUrl + "/uploads/profile-pictures/" + fileName;
    }

    public Path getProfilePicturePath(String fileName) {
        return Paths.get(profilePicturesDir).resolve(fileName);
    }
}
