package com.secureshare.service;

import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.util.Base64;

@Service
public class RSAService {

    public KeyPair generateKeyPair() throws Exception {

        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);

        return generator.generateKeyPair();
    }

    public byte[] hashFile(byte[] fileBytes) throws Exception {

        MessageDigest md = MessageDigest.getInstance("SHA-256");

        return md.digest(fileBytes);
    }

    public byte[] signHash(byte[] hash, PrivateKey privateKey) throws Exception {

        Cipher cipher = Cipher.getInstance("RSA");

        cipher.init(Cipher.ENCRYPT_MODE, privateKey);

        return cipher.doFinal(hash);
    }

    public byte[] verifySignature(byte[] signature, PublicKey publicKey) throws Exception {

        Cipher cipher = Cipher.getInstance("RSA");

        cipher.init(Cipher.DECRYPT_MODE, publicKey);

        return cipher.doFinal(signature);
    }

    public byte[] encryptPackage(byte[] data, PublicKey publicKey) throws Exception {

        Cipher cipher = Cipher.getInstance("RSA");

        cipher.init(Cipher.ENCRYPT_MODE, publicKey);

        return cipher.doFinal(data);
    }

    public byte[] decryptPackage(byte[] data, PrivateKey privateKey) throws Exception {

        Cipher cipher = Cipher.getInstance("RSA");

        cipher.init(Cipher.DECRYPT_MODE, privateKey);

        return cipher.doFinal(data);
    }

    public String toBase64(byte[] data) {
        return Base64.getEncoder().encodeToString(data);
    }
}