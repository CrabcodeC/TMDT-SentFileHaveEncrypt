package com.secureshare.controller;

import com.secureshare.service.FileService;
import com.secureshare.service.RSAService;
import com.secureshare.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.crypto.SecretKey;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@RestController
public class FileController {

    @Autowired
    private FileService fileService;

    @Autowired
    private RSAService rsaService;

    @Autowired
    private UserService userService;

    @PostMapping("/send")
    public Map<String, Object> sendFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("recipient") String recipientUsername,
            @RequestParam("sender") String senderUsername
    ) throws Exception {

        byte[] originalBytes = file.getBytes();

        // 1. Tạo Session Key AES
        SecretKey sessionKey = rsaService.generateAESKey();

        // 2. Mã hóa file bằng AES
        byte[] encryptedFileBytes = rsaService.encryptWithAES(originalBytes, sessionKey);

        // 3. Lấy Public Key của người nhận
        String pubKeyBase64 = userService.getPublicKeyBase64(recipientUsername);
        if (pubKeyBase64 == null) {
            throw new Exception("Không tìm thấy public key của người nhận: " + recipientUsername);
        }

        byte[] publicKeyBytes = Base64.getDecoder().decode(pubKeyBase64);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        PublicKey recipientPublicKey = kf.generatePublic(new X509EncodedKeySpec(publicKeyBytes));

        // 4. Mã hóa Session Key bằng Public Key người nhận (Phong bì số)
        byte[] encryptedSessionKey = rsaService.encryptWithPublicKey(sessionKey.getEncoded(), recipientPublicKey);

        // 5. Tạo Digital Signature
        byte[] hash = rsaService.hashFile(originalBytes);
        KeyPair senderKeyPair = rsaService.generateKeyPair(); // Tạm thời
        byte[] signature = rsaService.signHash(hash, senderKeyPair.getPrivate());

        // Lưu file đã mã hóa
        String savedPath = fileService.saveEncryptedFile(encryptedFileBytes, file.getOriginalFilename(), recipientUsername);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Gửi file thành công đến " + recipientUsername);
        response.put("fileName", file.getOriginalFilename());
        response.put("recipient", recipientUsername);
        response.put("encryptedSessionKey", rsaService.toBase64(encryptedSessionKey));
        response.put("signature", rsaService.toBase64(signature));
        response.put("hash", rsaService.toBase64(hash));
        response.put("savedPath", savedPath);

        return response;
    }
}