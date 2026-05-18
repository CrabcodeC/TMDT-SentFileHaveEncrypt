package com.secureshare.controller;

import com.secureshare.service.RSAService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.KeyPair;
import java.util.HashMap;
import java.util.Map;

@RestController
public class KeyController {

    @Autowired
    private RSAService rsaService;

    @GetMapping("/generate-key")
    public Map<String, String> generateKey() throws Exception {

        KeyPair pair = rsaService.generateKeyPair();

        Map<String, String> response = new HashMap<>();

        response.put("publicKey",
                rsaService.toBase64(pair.getPublic().getEncoded()));

        response.put("privateKey",
                rsaService.toBase64(pair.getPrivate().getEncoded()));

        return response;
    }
}