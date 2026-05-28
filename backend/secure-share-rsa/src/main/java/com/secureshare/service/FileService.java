package com.secureshare.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.charset.StandardCharsets;
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

    public String saveEncryptedFile(byte[] encryptedBytes, String originalName,
                                     String recipient, String sender,
                                     String encryptedSessionKey, String signature) throws Exception {
        File folder = new File(ENCRYPTED_DIR + recipient);
        if (!folder.exists()) folder.mkdirs();

        String fileName = "enc_" + originalName;
        Path path = Paths.get(ENCRYPTED_DIR + recipient + "/" + fileName);
        Files.write(path, encryptedBytes);

        // Lưu metadata (sender, sessionKey, signature) vào file .meta
        String metaContent = "sender=" + sender + "\n"
                + "encryptedSessionKey=" + encryptedSessionKey + "\n"
                + "signature=" + signature + "\n"
                + "originalName=" + originalName + "\n";
        Path metaPath = Paths.get(ENCRYPTED_DIR + recipient + "/" + fileName + ".meta");
        Files.write(metaPath, metaContent.getBytes(StandardCharsets.UTF_8));

        return path.toString();
    }
}
