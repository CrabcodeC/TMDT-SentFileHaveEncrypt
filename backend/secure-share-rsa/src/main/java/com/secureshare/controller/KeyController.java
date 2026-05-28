package com.secureshare.controller;

import com.secureshare.service.RSAService;
import com.secureshare.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyPair;
import java.util.HashMap;
import java.util.Map;

@RestController
public class KeyController {

    @Autowired
    private RSAService rsaService;

    @Autowired
    private UserService userService;

    // Tạo key tạm thời, chỉ trả về JSON, không lưu
    @GetMapping("/generate-key")
    public Map<String, String> generateKey() throws Exception {
        KeyPair pair = rsaService.generateKeyPair();
        Map<String, String> response = new HashMap<>();
        response.put("publicKey", rsaService.toBase64(pair.getPublic().getEncoded()));
        response.put("privateKey", rsaService.toBase64(pair.getPrivate().getEncoded()));
        return response;
    }

    // Tạo key MỚI cho user và lưu vào server
    @PostMapping("/api/createkey")
    public ResponseEntity<Map<String, Object>> createAndSaveKey(@RequestParam String username) {
        Map<String, Object> response = new HashMap<>();
        try {
            // Kiểm tra user có tồn tại không
            if (userService.findByUsername(username) == null) {
                response.put("error", "Không tìm thấy user: " + username);
                return ResponseEntity.badRequest().body(response);
            }

            // Tạo cặp khóa mới
            KeyPair pair = rsaService.generateKeyPair();
            String pubBase64  = rsaService.toBase64(pair.getPublic().getEncoded());
            String privBase64 = rsaService.toBase64(pair.getPrivate().getEncoded());

            // Lưu public key lên server (dùng để mã hóa khi nhận file)
            String pubPath  = "data/keys/" + username + "_public.key";
            String privPath = "data/keys/" + username + "_private.key";
            Files.write(Paths.get(pubPath),  pubBase64.getBytes(StandardCharsets.UTF_8));
            Files.write(Paths.get(privPath), privBase64.getBytes(StandardCharsets.UTF_8));

            response.put("message", "Tạo và lưu khóa thành công cho: " + username);
            response.put("username", username);
            response.put("publicKey", pubBase64);
            // Trả private key về để user tự lưu — server cũng giữ để demo giải mã
            response.put("privateKey", privBase64);
            response.put("publicKeyPath", pubPath);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            response.put("error", "Lỗi: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}
