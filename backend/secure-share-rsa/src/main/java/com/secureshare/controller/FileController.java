package com.secureshare.controller;

import com.secureshare.service.FileService;
import com.secureshare.service.RSAService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.KeyPair;
import java.util.HashMap;
import java.util.Map;

@RestController
public class FileController {

    @Autowired
    private FileService fileService;

    @Autowired
    private RSAService rsaService;

    @PostMapping("/send")
    public Map<String, String> sendFile(
            @RequestParam("file") MultipartFile file
    ) throws Exception {

        fileService.saveFile(file);

        byte[] fileBytes = file.getBytes();

        byte[] hash = rsaService.hashFile(fileBytes);

        KeyPair senderPair = rsaService.generateKeyPair();

        byte[] signature =
                rsaService.signHash(hash, senderPair.getPrivate());

        Map<String, String> response = new HashMap<>();

        response.put("message", "File Sent Successfully");
        response.put("hash", rsaService.toBase64(hash));
        response.put("signature", rsaService.toBase64(signature));

        return response;
    }
}