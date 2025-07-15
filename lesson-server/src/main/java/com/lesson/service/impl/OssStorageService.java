package com.lesson.service.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.GeneratePresignedUrlRequest;
import com.lesson.config.StorageProperties;
import com.lesson.service.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.net.URL;
import java.util.Date;

@Service
@ConditionalOnProperty(name = "storage.type", havingValue = "oss")
public class OssStorageService implements StorageService {
    @Autowired
    private StorageProperties storageProperties;

    private OSS getOssClient() {
        StorageProperties.Oss oss = storageProperties.getOss();
        return new OSSClientBuilder().build(
                oss.getEndpoint(),
                oss.getAccessKeyId(),
                oss.getAccessKeySecret()
        );
    }

    @Override
    public String getPresignedUploadUrl(String objectName, String contentType, long expireSeconds) {
        OSS ossClient = getOssClient();
        try {
            Date expiration = new Date(System.currentTimeMillis() + expireSeconds * 1000);
            GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(storageProperties.getOss().getBucketName(), objectName);
            request.setExpiration(expiration);
            request.setMethod(com.aliyun.oss.HttpMethod.PUT);
            request.setContentType(contentType);
            URL url = ossClient.generatePresignedUrl(request);
            return url.toString();
        } finally {
            ossClient.shutdown();
        }
    }

    @Override
    public String getPresignedDownloadUrl(String objectName, long expireSeconds) {
        OSS ossClient = getOssClient();
        try {
            Date expiration = new Date(System.currentTimeMillis() + expireSeconds * 1000);
            URL url = ossClient.generatePresignedUrl(storageProperties.getOss().getBucketName(), objectName, expiration, com.aliyun.oss.HttpMethod.GET);
            return url.toString();
        } finally {
            ossClient.shutdown();
        }
    }

    @Override
    public void uploadObject(String objectName, InputStream inputStream, String contentType) {
        OSS ossClient = getOssClient();
        try {
            ossClient.putObject(storageProperties.getOss().getBucketName(), objectName, inputStream);
        } finally {
            ossClient.shutdown();
        }
    }

    @Override
    public String getDownloadUrl(String objectName) {
        StorageProperties.Oss oss = storageProperties.getOss();
        return "https://" + oss.getBucketName() + "." + oss.getEndpoint().replaceFirst("https?://", "") + "/" + objectName;
    }

    @Override
    public void uploadFile(MultipartFile file, String objectName) {
        OSS ossClient = getOssClient();
        try {
            ossClient.putObject(storageProperties.getOss().getBucketName(), objectName, file.getInputStream());
        } catch (Exception e) {
            throw new RuntimeException("上传文件失败: " + e.getMessage(), e);
        } finally {
            ossClient.shutdown();
        }
    }
} 