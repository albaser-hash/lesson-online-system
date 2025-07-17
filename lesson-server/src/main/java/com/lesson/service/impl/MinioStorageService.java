package com.lesson.service.impl;

import com.lesson.config.StorageProperties;
import com.lesson.service.StorageService;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.http.Method;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.concurrent.TimeUnit;

@Service
@ConditionalOnProperty(name = "storage.type", havingValue = "minio", matchIfMissing = true)
public class MinioStorageService implements StorageService {
    @Autowired
    private MinioClient minioClient;
    @Autowired
    private StorageProperties storageProperties;

    /**
     * 生成预签名的上传URL
     * 该方法用于获取一个预签名的URL，以便客户端可以直接上传文件到指定的存储桶，而无需经过服务器中转
     * 主要应用于需要临时提供对对象存储的写权限，而又不想暴露存储桶的访问密钥的场景
     *
     * @param objectName 存储在对象存储中的文件名，用于指定上传的目标位置
     * @param contentType 文件的MIME类型，指示上传文件的格式
     * @param expireSeconds 预签名URL的有效期，单位为秒，决定了URL可以在多长时间内用于上传
     * @return 返回一个预签名的URL字符串，通过该URL可以在指定时间内上传文件
     * @throws RuntimeException 如果生成预签名URL的过程中发生错误，例如网络问题、配置错误等，将抛出运行时异常
     */
    @Override
    public String getPresignedUploadUrl(String objectName, String contentType, long expireSeconds) {
        try {
            // 使用MinIO Java SDK的getPresignedObjectUrl方法生成预签名的上传URL
            return minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                    .method(Method.PUT) // 指定HTTP方法为PUT，用于上传文件
                    .bucket(storageProperties.getMinio().getBucketName()) // 指定上传的目标存储桶
                    .object(objectName) // 指定上传文件在存储桶中的名称
                    .expiry((int)expireSeconds, TimeUnit.SECONDS) // 设置预签名URL的有效期
                    .build()
            );
        } catch (Exception e) {
            // 如果在生成预签名URL的过程中发生任何异常，将其包装为RuntimeException并重新抛出
            throw new RuntimeException("生成上传签名失败: " + e.getMessage(), e);
        }
    }
    /**
     * 获取预签名的下载URL
     *
     * 通过对象名称和过期时间生成一个预签名的下载URL，用于临时访问MinIO中的对象
     * 这个URL可以在指定的时间内用于下载对象，而不需要提供额外的身份验证信息
     *
     * @param objectName 存储在MinIO中的对象名称
     * @param expireSeconds 预签名URL在生成后的有效秒数
     * @return 预签名的下载URL字符串
     * @throws RuntimeException 如果生成预签名URL时发生错误，则抛出运行时异常
     */
    @Override
    public String getPresignedDownloadUrl(String objectName, long expireSeconds) {
        try {
            // 使用MinIO客户端生成预签名的URL
            return minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                    .method(Method.GET) // 指定HTTP方法为GET，表示下载操作
                    .bucket(storageProperties.getMinio().getBucketName()) // 设置存储桶名称
                    .object(objectName) // 设置对象名称
                    .expiry((int)expireSeconds, TimeUnit.SECONDS) // 设置URL过期时间
                    .build()
            );
        } catch (Exception e) {
            // 如果生成预签名URL时发生错误，抛出自定义的运行时异常
            throw new RuntimeException("生成下载签名失败: " + e.getMessage(), e);
        }
    }

    /**
     * 上传对象到MinIO服务器
     *
     * @param objectName 存储在MinIO服务器上的对象名称
     * @param inputStream 输入流，用于读取上传的对象数据
     * @param contentType 对象的MIME类型
     *
     * 此方法负责将给定的数据流作为对象上传到配置的MinIO桶中
     * 它使用MinIO Java SDK的putObject方法来执行上传操作
     * 如果上传过程中遇到任何异常，将抛出运行时异常
     */
    @Override
    public void uploadObject(String objectName, InputStream inputStream, String contentType) {
        try {
            // 使用MinIO客户端将对象上传到MinIO服务器
            minioClient.putObject(
                PutObjectArgs.builder()
                    .bucket(storageProperties.getMinio().getBucketName())
                    .object(objectName)
                    .stream(inputStream, -1, 10485760)
                    .contentType(contentType)
                    .build()
            );
        } catch (Exception e) {
            // 如果上传过程中发生异常，抛出运行时异常
            throw new RuntimeException("上传对象失败: " + e.getMessage(), e);
        }
    }

    @Override
    public String getDownloadUrl(String objectName) {
        return storageProperties.getMinio().getEndpoint() + "/" + storageProperties.getMinio().getBucketName() + "/" + objectName;
    }

    @Override
    public void uploadFile(MultipartFile file, String objectName) {
        try {
            minioClient.putObject(
                PutObjectArgs.builder()
                    .bucket(storageProperties.getMinio().getBucketName())
                    .object(objectName)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build()
            );
        } catch (Exception e) {
            throw new RuntimeException("上传文件失败: " + e.getMessage(), e);
        }
    }
} 