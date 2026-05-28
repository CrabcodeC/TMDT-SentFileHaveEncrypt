package com.secureshare.controller;

import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class InboxController {

    private final String ENCRYPTED_DIR = "encrypted/";

    // API: trả về JSON danh sách file trong inbox
    @GetMapping("/api/inbox")
    public List<Map<String, String>> getInbox(@RequestParam String username) {
        List<Map<String, String>> files = new ArrayList<>();
        File folder = new File(ENCRYPTED_DIR + username);

        if (folder.exists() && folder.isDirectory()) {
            File[] fileList = folder.listFiles();
            if (fileList != null) {
                for (File file : fileList) {
                    if (file.isFile()) {
                        Map<String, String> fileInfo = new HashMap<>();
                        fileInfo.put("fileName", file.getName());
                        fileInfo.put("sender", "Unknown");
                        fileInfo.put("time", "Vừa xong");
                        fileInfo.put("path", file.getAbsolutePath());
                        files.add(fileInfo);
                    }
                }
            }
        }
        return files;
    }

    @GetMapping("/download")
    public ResponseEntity<FileSystemResource> downloadFile(
            @RequestParam String path,
            @RequestParam String name) {

        File file = new File(path);
        if (!file.exists()) {
            return ResponseEntity.notFound().build();
        }

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + name + "\"");

        return ResponseEntity.ok()
                .headers(headers)
                .body(new FileSystemResource(file));
    }
}
