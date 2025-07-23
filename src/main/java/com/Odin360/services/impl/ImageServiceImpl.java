package com.Odin360.services.impl;

import com.Odin360.repositories.UserRepository;
import com.Odin360.services.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.Odin360.Domains.entities.User;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Logger; // Using java.util.logging for simplicity

@Service
public class ImageServiceImpl implements ImageService {
   private final UserRepository userRepository;
    private static final Logger log = Logger.getLogger(ImageServiceImpl.class.getName());

    @Value("${app.upload.dir:./uploads}")
    private String uploadBaseDir;

    // Constructor: This is where we'll ensure the base upload directory exists.
    // Spring will inject 'uploadBaseDir' before calling this constructor,
    // provided the bean is managed by Spring (which it is, due to @Service).
    public ImageServiceImpl(UserRepository userRepository, @Value("${app.upload.dir:./uploads}") String uploadBaseDir) {
        this.userRepository = userRepository;
        this.uploadBaseDir = uploadBaseDir;
        try {
            Path uploadPath = Paths.get(this.uploadBaseDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                log.info("Created upload directory: " + uploadPath.toAbsolutePath());
            }
        } catch (IOException e) {
            // Log the error and throw a RuntimeException to prevent the application from starting
            // if the directory cannot be created. This is critical for the service to function.
            log.severe("CRITICAL ERROR: Could not create upload directory: " + this.uploadBaseDir + " - " + e.getMessage());
            throw new RuntimeException("Could not initialize storage directory. Application cannot start.", e);
        }
    }

    @Override
    public void uploadImage(MultipartFile file, UUID userId) throws IOException {
        // Validate input
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("No file was provided or the file is empty.");
        }


        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String savedFileName = userId + extension;
        Path targetFilePath = Paths.get(uploadBaseDir, savedFileName);


        // IMPORTANT SECURITY: Validate originalFilename to prevent path traversal
        // Ensure filename does not contain path separators or special characters that could
        // lead to accessing files outside the intended directory.
     //   if (originalFilename.contains("..") || originalFilename.contains("/") || originalFilename.contains("\\") || originalFilename.contains("\0")) {
       //     throw new IllegalArgumentException("Invalid filename provided. Filename cannot contain path separators, '..', or null characters.");
        //}
        // Optionally, restrict characters further (e.g., only alphanumeric, hyphens, underscores, dots)
        // if (!originalFilename.matches("[a-zA-Z0-9_.-]+")) {
        //     throw new IllegalArgumentException("Filename contains invalid characters.");
        // }


        // 1. Create a user-specific subdirectory
        // We resolve this path relative to the base upload directory


        // Ensure the user's directory exists
        if (!Files.exists(targetFilePath)) {
            Files.createDirectories(targetFilePath);
            log.info("Created user-specific directory: " + targetFilePath.toAbsolutePath());
        }

        // 2. Construct the full target path using the original filename
        // This is safe as Paths.get handles concatenation and normalization securely.
       // Path targetFilePath = userUploadDirPath.resolve(originalFilename);
        User user = userRepository.findById(userId)
                .orElseThrow(()->new RuntimeException("user not found"));
        user.setProfileImage(savedFileName);
        userRepository.save(user);
        // 3. Copy the file to the target location
        Files.copy(file.getInputStream(), targetFilePath, StandardCopyOption.REPLACE_EXISTING);

        log.info("File '" + originalFilename + "' successfully copied to: " + targetFilePath.toAbsolutePath());

        // In a real application, you'd typically save 'userId' and 'originalFilename'
        // to a database here so you can retrieve the file later.
        // For example:
        // FileMetadata metadata = new FileMetadata(UUID.randomUUID().toString(), userId, originalFilename, file.getContentType(), file.getSize(), LocalDateTime.now());
        // fileMetadataRepository.save(metadata);
    }

    @Override
    public File downloadImage( UUID userId) throws IOException {



       User user = userRepository.findById(userId)
               .orElseThrow(()->new RuntimeException("User not found"));
        String savedFileName = user.getProfileImage();
        Path targetFilePath = Paths.get(uploadBaseDir, savedFileName);


        // --- DEBUGGING LOGS ---
        log.info("Download Request Debug:");
        log.info("  Requested Path (from input): " + targetFilePath);

        File fileToDownload = targetFilePath.toFile();

        // 3. Check if the file exists and is a regular file (not a directory)
        if (!fileToDownload.exists() || !fileToDownload.isFile()) {
            throw new FileNotFoundException("No file named  was found for user '" + userId + "'.");
        }

        return fileToDownload;
    }
}