package com.Odin360.services.impl;

import com.Odin360.services.ImageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.logging.Logger; // Using java.util.logging for simplicity

@Service
public class ImageServiceImpl implements ImageService {

    private static final Logger log = Logger.getLogger(ImageServiceImpl.class.getName());

    @Value("${app.upload.dir:./uploads}")
    private String uploadBaseDir;

    // Constructor: This is where we'll ensure the base upload directory exists.
    // Spring will inject 'uploadBaseDir' before calling this constructor,
    // provided the bean is managed by Spring (which it is, due to @Service).
    public ImageServiceImpl(@Value("${app.upload.dir:./uploads}") String uploadBaseDir) {
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
    public void uploadImage(MultipartFile file, String userId) throws IOException {
        // Validate input
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("No file was provided or the file is empty.");
        }
        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("User ID must be provided.");
        }

        String originalFilename = Objects.requireNonNull(file.getOriginalFilename());

        // IMPORTANT SECURITY: Validate originalFilename to prevent path traversal
        // Ensure filename does not contain path separators or special characters that could
        // lead to accessing files outside the intended directory.
        if (originalFilename.contains("..") || originalFilename.contains("/") || originalFilename.contains("\\") || originalFilename.contains("\0")) {
            throw new IllegalArgumentException("Invalid filename provided. Filename cannot contain path separators, '..', or null characters.");
        }
        // Optionally, restrict characters further (e.g., only alphanumeric, hyphens, underscores, dots)
        // if (!originalFilename.matches("[a-zA-Z0-9_.-]+")) {
        //     throw new IllegalArgumentException("Filename contains invalid characters.");
        // }


        // 1. Create a user-specific subdirectory
        // We resolve this path relative to the base upload directory
        Path userUploadDirPath = Paths.get(uploadBaseDir, userId);

        // Ensure the user's directory exists
        if (!Files.exists(userUploadDirPath)) {
            Files.createDirectories(userUploadDirPath);
            log.info("Created user-specific directory: " + userUploadDirPath.toAbsolutePath());
        }

        // 2. Construct the full target path using the original filename
        // This is safe as Paths.get handles concatenation and normalization securely.
        Path targetFilePath = userUploadDirPath.resolve(originalFilename);

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
    public File downloadImage(String originalFilename, String userId) throws IOException {
        // Validate input
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            throw new IllegalArgumentException("Filename must be provided.");
        }
        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("User ID must be provided.");
        }
        // IMPORTANT SECURITY: Validate originalFilename to prevent path traversal
        if (originalFilename.contains("..") || originalFilename.contains("/") || originalFilename.contains("\\") || originalFilename.contains("\0")) {
            throw new IllegalArgumentException("Invalid filename provided. Filename cannot contain path separators, '..', or null characters.");
        }

        // 1. Construct the path to the file based on the user ID and original filename
        Path targetFilePath = Paths.get(uploadBaseDir, userId, originalFilename);

        // 2. IMPORTANT SECURITY STEP: Normalize and resolve the path
        // This ensures no '..' (path traversal) or symbolic link attacks can occur.
        // It guarantees the path refers to a file *within* your intended base directory.
        Path normalizedPath = targetFilePath.normalize();
        Path absoluteNormalizedPath = normalizedPath.toAbsolutePath();

        // Get absolute paths for comparison
        Path absoluteBaseUploadDir = Paths.get(uploadBaseDir).toAbsolutePath().normalize();
        Path absoluteUserUploadDir = Paths.get(uploadBaseDir, userId).toAbsolutePath().normalize();

        // --- DEBUGGING LOGS ---
        log.info("Download Request Debug:");
        log.info("  Requested Path (from input): " + targetFilePath);
        log.info("  Absolute Normalized Path: " + absoluteNormalizedPath);
        log.info("  Absolute Base Upload Dir: " + absoluteBaseUploadDir);
        log.info("  Absolute User Upload Dir: " + absoluteUserUploadDir);
        log.info("  absoluteNormalizedPath.startsWith(absoluteUserUploadDir): " + absoluteNormalizedPath.startsWith(absoluteUserUploadDir));
        log.info("  absoluteNormalizedPath.startsWith(absoluteBaseUploadDir): " + absoluteNormalizedPath.startsWith(absoluteBaseUploadDir));
        // --- END DEBUGGING LOGS ---

        // Additional check: Ensure the file being accessed is truly within the *user's specific* directory
        // AND within the overall base upload directory.
        // Both conditions must be true for the path to be valid.
        // If either startsWith check returns false, it means the path is outside the expected boundaries.
        if (!absoluteNormalizedPath.startsWith(absoluteUserUploadDir) || !absoluteNormalizedPath.startsWith(absoluteBaseUploadDir)) {
            log.warning("Security violation during file download for user " + userId + " and filename " + originalFilename + ": Attempted to access a file outside the designated directory.");
            throw new SecurityException("Access denied: Attempted to access a file outside the designated directory.");
        }
        // Also, ensure the canonical path doesn't point to a directory itself, if only files are expected.
        if (Files.isDirectory(absoluteNormalizedPath)) {
            throw new SecurityException("Cannot download a directory.");
        }

        File fileToDownload = absoluteNormalizedPath.toFile();

        // 3. Check if the file exists and is a regular file (not a directory)
        if (!fileToDownload.exists() || !fileToDownload.isFile()) {
            throw new FileNotFoundException("No file named '" + originalFilename + "' was found for user '" + userId + "'.");
        }

        return fileToDownload;
    }
}