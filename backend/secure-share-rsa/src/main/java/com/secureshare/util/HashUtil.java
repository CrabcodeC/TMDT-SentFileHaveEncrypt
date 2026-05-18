package com.secureshare.util;

import java.security.MessageDigest;

public class HashUtil {

    public static byte[] sha256(byte[] data)
            throws Exception {

        MessageDigest md =
                MessageDigest.getInstance("SHA-256");

        return md.digest(data);
    }
}