package com.secureshare.service;

import com.secureshare.model.User;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Service
public class UserService {

    private final String USERS_FILE = "data/users.txt";

    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        try (BufferedReader br = Files.newBufferedReader(Paths.get(USERS_FILE), StandardCharsets.UTF_8)) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                String[] parts = line.split("\\|");
                if (parts.length == 4) {
                    users.add(new User(parts[0].trim(), parts[1].trim(), parts[2].trim(), parts[3].trim()));
                }
            }
        } catch (IOException e) {
            System.err.println("Không đọc được file users: " + e.getMessage());
        }
        return users;
    }

    public User findByUsername(String username) {
        return getAllUsers().stream()
                .filter(u -> u.getUsername().equals(username))
                .findFirst()
                .orElse(null);
    }

    public boolean authenticate(String username, String password) {
        User user = findByUsername(username);
        return user != null && user.getPassword().equals(password);
    }

    public String getPublicKeyBase64(String username) throws Exception {
        User user = findByUsername(username);
        if (user == null) return null;
        return new String(Files.readAllBytes(Paths.get(user.getPublicKeyPath())), StandardCharsets.UTF_8).trim();
    }

    public KeyPair getKeyPair(String username) {
        try {
            String pubKeyBase64 = getPublicKeyBase64(username);
            if (pubKeyBase64 == null) return null;

            // Thử đọc private key (nếu có file tương ứng)
            String privKeyPath = "data/keys/" + username + "_private.key";
            if (!Files.exists(Paths.get(privKeyPath))) return null;

            String privKeyBase64 = new String(Files.readAllBytes(Paths.get(privKeyPath)), StandardCharsets.UTF_8).trim();

            KeyFactory kf = KeyFactory.getInstance("RSA");
            PublicKey pub = kf.generatePublic(new X509EncodedKeySpec(Base64.getDecoder().decode(pubKeyBase64)));
            PrivateKey priv = kf.generatePrivate(new PKCS8EncodedKeySpec(Base64.getDecoder().decode(privKeyBase64)));
            return new KeyPair(pub, priv);
        } catch (Exception e) {
            return null;
        }
    }
}
