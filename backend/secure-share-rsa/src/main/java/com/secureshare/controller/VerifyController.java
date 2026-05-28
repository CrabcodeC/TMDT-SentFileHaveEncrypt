package com.secureshare.controller;

import com.secureshare.service.RSAService;
import com.secureshare.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@RestController
public class VerifyController {

    @Autowired
    private RSAService rsaService;

    @Autowired
    private UserService userService;

    /**
     * Xác thực và giải mã file:
     * 1. Nhận: file mã hóa + encryptedSessionKey + signature + tên người gửi + tên người nhận
     * 2. Dùng private key người nhận → giải mã sessionKey
     * 3. Dùng sessionKey → giải mã file
     * 4. Dùng public key người gửi → xác thực chữ ký
     * 5. So sánh hash → kiểm tra toàn vẹn
     */
    @PostMapping("/api/verify")
    public ResponseEntity<Map<String, Object>> verifyAndDecrypt(
            @RequestParam("file") MultipartFile encryptedFile,
            @RequestParam("encryptedSessionKey") String encryptedSessionKeyB64,
            @RequestParam("signature") String signatureB64,
            @RequestParam("sender") String senderUsername,
            @RequestParam("recipient") String recipientUsername
    ) {
        Map<String, Object> response = new HashMap<>();
        try {
            // ── Bước 1: Lấy private key người nhận ──────────────────────────
            KeyPair recipientKeyPair = userService.getKeyPair(recipientUsername);
            if (recipientKeyPair == null) {
                response.put("error", "Không tìm thấy private key của người nhận: " + recipientUsername);
                return ResponseEntity.badRequest().body(response);
            }

            // ── Bước 2: Giải mã Session Key bằng private key người nhận ─────
            byte[] encryptedSessionKeyBytes = Base64.getDecoder().decode(encryptedSessionKeyB64.trim());
            byte[] sessionKeyBytes = rsaService.decryptWithPrivateKey(encryptedSessionKeyBytes, recipientKeyPair.getPrivate());
            SecretKey sessionKey = rsaService.base64ToAESKey(Base64.getEncoder().encodeToString(sessionKeyBytes));

            // ── Bước 3: Giải mã file bằng AES Session Key ───────────────────
            byte[] encryptedFileBytes = encryptedFile.getBytes();
            byte[] decryptedFileBytes = rsaService.decryptWithAES(encryptedFileBytes, sessionKey);

            // ── Bước 4: Lấy public key người gửi để xác thực chữ ký ─────────
            String senderPubKeyB64 = userService.getPublicKeyBase64(senderUsername);
            if (senderPubKeyB64 == null) {
                response.put("error", "Không tìm thấy public key của người gửi: " + senderUsername);
                return ResponseEntity.badRequest().body(response);
            }
            KeyFactory kf = KeyFactory.getInstance("RSA");
            PublicKey senderPublicKey = kf.generatePublic(
                new X509EncodedKeySpec(Base64.getDecoder().decode(senderPubKeyB64.trim()))
            );

            // ── Bước 5: Xác thực chữ ký số ──────────────────────────────────
            byte[] signatureBytes  = Base64.getDecoder().decode(signatureB64.trim());
            byte[] hashFromSig     = rsaService.verifySignature(signatureBytes, senderPublicKey);
            byte[] hashOfDecrypted = rsaService.hashFile(decryptedFileBytes);
            boolean signatureValid = Arrays.equals(hashFromSig, hashOfDecrypted);

            // ── Bước 6: Lưu file đã giải mã ─────────────────────────────────
            String outDir  = "decrypted/" + recipientUsername + "/";
            Files.createDirectories(Paths.get(outDir));
            String originalName = encryptedFile.getOriginalFilename()
                    .replaceFirst("^enc_", ""); // bỏ prefix enc_
            String outPath = outDir + "dec_" + originalName;
            Files.write(Paths.get(outPath), decryptedFileBytes);

            // ── Kết quả ──────────────────────────────────────────────────────
            response.put("signatureValid",  signatureValid);
            response.put("integrityOk",     signatureValid);
            response.put("sender",          senderUsername);
            response.put("recipient",       recipientUsername);
            response.put("originalName",    originalName);
            response.put("decryptedPath",   outPath);
            response.put("fileSizeBytes",   decryptedFileBytes.length);
            response.put("hashHex",         bytesToHex(hashOfDecrypted));
            response.put("message", signatureValid
                    ? "✅ Xác thực thành công! File toàn vẹn, chữ ký hợp lệ."
                    : "⚠️ File đã bị thay đổi hoặc chữ ký không hợp lệ!");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            response.put("error", "Lỗi xác thực/giải mã: " + e.getMessage());
            response.put("signatureValid", false);
            return ResponseEntity.internalServerError().body(response);
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) sb.append(String.format("%02x", b));
        return sb.toString();
    }
}
