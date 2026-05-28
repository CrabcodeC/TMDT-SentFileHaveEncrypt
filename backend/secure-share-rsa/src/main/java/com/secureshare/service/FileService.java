package com.secureshare.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class FileService {

    private final String UPLOAD_DIR = "uploads/";
    private final String ENCRYPTED_DIR = "encrypted/";

    public String saveFile(MultipartFile file) throws Exception {
        File folder = new File(UPLOAD_DIR);
        if (!folder.exists()) folder.mkdirs();

        Path path = Paths.get(UPLOAD_DIR + file.getOriginalFilename());
        Files.write(path, file.getBytes());
        return path.toString();
    }

    public String saveEncryptedFile(byte[] encryptedBytes, String originalName, String recipient) throws Exception {
        File folder = new File(ENCRYPTED_DIR + recipient);
        if (!folder.exists()) folder.mkdirs();

        String fileName = "enc_" + originalName;
        Path path = Paths.get(ENCRYPTED_DIR + recipient + "/" + fileName);
        Files.write(path, encryptedBytes);
        return path.toString();
    }
}