package com.secureshare.util;

import java.nio.file.Files;
import java.nio.file.Paths;

public class FileUtil {

    public static byte[] readFile(String path)
            throws Exception {

        return Files.readAllBytes(
                Paths.get(path)
        );
    }
}