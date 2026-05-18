package com.secureshare.util;

import java.nio.file.Files;
import java.nio.file.Paths;

public class KeyUtil {

    public static void saveKey(
            String path,
            byte[] key
    ) throws Exception {

        Files.write(Paths.get(path), key);
    }
}