package com.secureshare.service;

import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
public class VerifyService {

    public boolean compareHash(byte[] hash1, byte[] hash2) {

        return Arrays.equals(hash1, hash2);
    }
}