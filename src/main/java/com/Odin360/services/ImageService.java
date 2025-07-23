package com.Odin360.services;


import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.UUID;


public interface ImageService {
    void uploadImage(MultipartFile file, UUID userId) throws IOException;

    File downloadImage(UUID userId) throws IOException;
}
