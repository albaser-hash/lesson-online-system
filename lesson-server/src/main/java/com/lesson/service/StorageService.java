package com.lesson.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

public interface StorageService {
    String getPresignedUploadUrl(String objectName, String contentType, long expireSeconds);
    String getPresignedDownloadUrl(String objectName, long expireSeconds);
    void uploadObject(String objectName, InputStream inputStream, String contentType);
    void uploadFile(MultipartFile file, String objectName);
    String getDownloadUrl(String objectName);
} 