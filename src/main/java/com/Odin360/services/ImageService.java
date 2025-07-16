package com.Odin360.services;


import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public interface ImageService {
    void uploadImage(MultipartFile file,String userId) throws IOException;

    File downloadImage(String file, String userId) throws IOException;
}
