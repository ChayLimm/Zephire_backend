package com.example.HrAssistance.service;

import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

public interface StorageService {

    // ─────────────────────────────────────────
    // Save file to disk
    // ─────────────────────────────────────────
    String saveFile(MultipartFile file) ;
    void deleteFile(String fileUrl);
}
