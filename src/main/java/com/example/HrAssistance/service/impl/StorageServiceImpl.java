package com.example.HrAssistance.service.impl;

import com.example.HrAssistance.service.StorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Slf4j
@Service
public class StorageServiceImpl  implements StorageService {
    @Autowired
    private S3Client s3Client;

    @Value("${cloudflare.r2.bucket}")
    private String bucket;

    @Value("${cloudflare.r2.public-domain}")
    private String publicDomain;
    // ─────────────────────────────────────────
    // Save file to disk
    // ─────────────────────────────────────────
    public String saveFile(MultipartFile file) {
        try {
            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();

            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucket)
                            .key(fileName)
                            .contentType(file.getContentType())
                            .build(),
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize())
            );

            String fileUrl =  publicDomain + "/" + fileName;
            log.info("✅ File uploaded to R2: {}", fileUrl);
            return fileUrl;

        } catch (Exception e) {
            log.error("❌ Failed to upload to R2: {}", e.getMessage());
            return null;
        }
    }

     public void deleteFile(String fileUrl) {
        try {
            if (fileUrl != null) {
                // extract just the key from the full URL
                String fileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
                s3Client.deleteObject(builder -> builder.bucket(bucket).key(fileName).build());
                log.info("File deleted from R2: {}", fileName);
            }
        } catch (Exception e) {
            log.warn("⚠Could not delete file from R2: {}", e.getMessage());
        }
    }
}
