package com.secureshare.controller;

import com.secureshare.service.FileService;
import com.secureshare.service.RSAService;
import com.secureshare.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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

    @PostMapping("/api/send")
    public ResponseEntity<Map<String, Object>> sendFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("recipient") String recipientUsername,
            @RequestParam("sender") String senderUsername
    ) {
        Map<String, Object> response = new HashMap<>();
        try {
            byte[] originalBytes = file.getBytes();

            // 1. Tạo Session Key AES
            SecretKey sessionKey = rsaService.generateAESKey();

            // 2. Mã hóa file bằng AES
            byte[] encryptedFileBytes = rsaService.encryptWithAES(originalBytes, sessionKey);

            // 3. Lấy Public Key của người nhận
            String pubKeyBase64 = userService.getPublicKeyBase64(recipientUsername);
            if (pubKeyBase64 == null) {
                response.put("error", "Không tìm thấy public key của người nhận: " + recipientUsername);
                return ResponseEntity.badRequest().body(response);
            }

            byte[] publicKeyBytes = Base64.getDecoder().decode(pubKeyBase64.trim());
            KeyFactory kf = KeyFactory.getInstance("RSA");
            PublicKey recipientPublicKey = kf.generatePublic(new X509EncodedKeySpec(publicKeyBytes));

            // 4. Mã hóa Session Key bằng Public Key người nhận (Phong bì số)
            byte[] encryptedSessionKey = rsaService.encryptWithPublicKey(sessionKey.getEncoded(), recipientPublicKey);

            // 5. Lấy Private Key người gửi để ký số
            KeyPair senderKeyPair = userService.getKeyPair(senderUsername);
            byte[] hash = rsaService.hashFile(originalBytes);
            byte[] signature;
            if (senderKeyPair != null) {
                signature = rsaService.signHash(hash, senderKeyPair.getPrivate());
            } else {
                // Fallback: tạo keypair tạm
                senderKeyPair = rsaService.generateKeyPair();
                signature = rsaService.signHash(hash, senderKeyPair.getPrivate());
            }

            // 6. Lưu file đã mã hóa + metadata
            String savedPath = fileService.saveEncryptedFile(
                encryptedFileBytes, file.getOriginalFilename(), recipientUsername,
                senderUsername, rsaService.toBase64(encryptedSessionKey), rsaService.toBase64(signature)
            );

            response.put("message", "Gửi file thành công đến " + recipientUsername);
            response.put("fileName", file.getOriginalFilename());
            response.put("recipient", recipientUsername);
            response.put("sender", senderUsername);
            response.put("encryptedSessionKey", rsaService.toBase64(encryptedSessionKey));
            response.put("signature", rsaService.toBase64(signature));
            response.put("hash", rsaService.toBase64(hash));
            response.put("savedPath", savedPath);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            response.put("error", "Lỗi: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}
