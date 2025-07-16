package com.Odin360.controllers;

import com.Odin360.services.ImageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders; // Import for setting content-disposition
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URLEncoder; // For URL encoding filenames
import java.nio.charset.StandardCharsets; // For URL encoding character set
import java.nio.file.Files;

@Slf4j // Provides a logger instance
@RestController
@RequestMapping("/api/v1/image")
@RequiredArgsConstructor // Generates a constructor with required arguments (final fields)
public class ImageController {

    private final ImageService imageService;

    /**
     * Handles image uploads.
     * Requires a 'file' parameter (MultipartFile) and a 'userId' in the path.
     *
     * @param file The image file to upload.
     * @param userId The ID of the user uploading the image.
     * @return ResponseEntity indicating success or failure.
     */
    @PostMapping("/upload/{userId}")
    public ResponseEntity<String> uploadImage(
            @RequestParam("file") MultipartFile file,
            @PathVariable String userId) {
        try {
            imageService.uploadImage(file, userId);
            log.info("Image uploaded successfully for user: {}", userId);
            return ResponseEntity.ok("Image has been uploaded successfully.");
        } catch (IllegalArgumentException e) {
            // Catches validation errors from the service (e.g., file empty, userId empty)
            log.warn("Bad request for image upload (User ID: {}): {}", userId, e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (SecurityException e) {
            // Catches security-related issues (e.g., path traversal attempts if still present in future modifications)
            log.error("Security violation during image upload for user {}: {}", userId, e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Security error during upload: " + e.getMessage());
        } catch (IOException e) {
            // Catches general I/O errors during file processing
            log.error("IO Exception during image upload for user {}: {}", userId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to upload image due to server error.");
        } catch (Exception e) {
            // Catches any other unexpected runtime exceptions
            log.error("An unexpected error occurred during image upload for user {}: {}", userId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred during upload.");
        }
    }

    /**
     * Handles image downloads.
     * Requires a 'storedFileName' as a request parameter and a 'userId' in the path.
     *
     * @param storedFileName The unique filename of the image as stored on the server.
     * @param userId The ID of the user who owns the image.
     * @return ResponseEntity containing the image file as a resource.
     */
    @GetMapping("/download/{userId}")
    public ResponseEntity<Resource> downloadImage(
            @RequestParam("file") String storedFileName, // Renamed param for clarity with service
            @PathVariable String userId) {
        try {
            File fileToDownload = imageService.downloadImage(storedFileName, userId);

            // Determine content type dynamically
            String contentType = Files.probeContentType(fileToDownload.toPath());
            if (contentType == null) {
                contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE; // Fallback to generic binary stream
            }

            // Set Content-Disposition header to suggest filename for download
            String originalDownloadFileName = storedFileName; // You might get the original name from a DB if stored
            String encodedFileName = URLEncoder.encode(originalDownloadFileName, StandardCharsets.UTF_8.toString()).replace("+", "%20");
            String headerValue = "attachment; filename=\"" + encodedFileName + "\"; filename*=UTF-8''" + encodedFileName;

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType)) // Set dynamic content type
                    .contentLength(fileToDownload.length())              // Set file size
                    .header(HttpHeaders.CONTENT_DISPOSITION, headerValue) // Suggest filename for browser
                    .body(new FileSystemResource(fileToDownload));       // Return file as resource

        } catch (FileNotFoundException e) {
            // Catches specific error if the file does not exist
            log.warn("File not found during download: User ID={}, Stored Filename={}", userId, storedFileName);
            return ResponseEntity.notFound().build(); // 404 Not Found
        } catch (IllegalArgumentException e) {
            // Catches validation errors from the service (e.g., filename/userId empty)
            log.warn("Bad request for image download (User ID: {}, Filename: {}): {}", userId, storedFileName, e.getMessage());
            return ResponseEntity.badRequest().body(null); // 400 Bad Request (no body for Resource type)
        } catch (SecurityException e) {
            // Catches security-related issues (e.g., path traversal detected)
            log.error("Security violation during image download for user {}: {}", userId, e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null); // 403 Forbidden (no body for Resource type)
        } catch (IOException e) {
            // Catches general I/O errors during file processing
            log.error("IO Exception during image download for user {}: {}", userId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null); // 500 Internal Server Error
        } catch (Exception e) {
            // Catches any other unexpected runtime exceptions
            log.error("An unexpected error occurred during image download for user {}: {}", userId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}