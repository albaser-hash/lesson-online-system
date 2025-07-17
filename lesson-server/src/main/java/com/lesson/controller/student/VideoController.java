package com.lesson.controller.student;

import com.lesson.entity.Chapter;
import com.lesson.mapper.ChapterMapper;
import com.lesson.service.UserService;
import com.lesson.result.Result;
import com.lesson.service.StorageService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class VideoController {
    @Autowired
    private ChapterMapper chapterMapper;
    @Autowired
    private UserService userService;
    @Autowired
    private StorageService storageService;

    @ApiOperation("获取视频临时播放地址")
    @GetMapping("/api/video/play")
    public Result<String> getPlayUrl(@RequestParam Integer chapterId, @RequestParam Integer userId) {
        // 1. 查章节
        Chapter chapter = chapterMapper.selectById(chapterId);
        if (chapter == null) return Result.error("章节不存在");
        String videoUrl = chapter.getVideoUrl();
        String durationStr = chapter.getVideoDuration();
        if (!userService.canPlayChapter(userId, chapter)) {
            return Result.error("无权限观看");
        }
        // 3. 计算签名有效期
        int durationSeconds = parseDurationToSeconds(durationStr);
        int expireSeconds = durationSeconds + 3600; // 多1小时
        // 4. 生成签名URL（通过 StorageService 统一接口）
        try {
            // 从完整的OSS URL中提取对象名
            String objectName = extractObjectNameFromUrl(videoUrl);
            String signedUrl = storageService.getPresignedDownloadUrl(objectName, expireSeconds);
            return Result.success(signedUrl);
        } catch (Exception e) {
            return Result.error("生成视频播放地址失败: " + e.getMessage());
        }
    }

    // 工具方法：将 00:30:00 转为秒
    private int parseDurationToSeconds(String duration) {
        if (duration == null || duration.isEmpty()) return 3600;
        String[] parts = duration.split(":");
        int h = Integer.parseInt(parts[0]);
        int m = Integer.parseInt(parts[1]);
        int s = Integer.parseInt(parts[2]);
        return h * 3600 + m * 60 + s;
    }
    
    // 从完整的OSS URL中提取对象名
    private String extractObjectNameFromUrl(String url) {
        if (url == null || url.isEmpty()) {
            throw new IllegalArgumentException("URL不能为空");
        }
        
        // 如果是完整的OSS URL，提取对象名
        if (url.startsWith("http://") || url.startsWith("https://")) {
            // 从URL中提取对象名，例如：
            // https://lesson-online.oss-cn-beijing.aliyuncs.com/videos/1752209878131_Screenrecorder-2025-07-10-18-08-33-781.mp4
            // 提取出：videos/1752209878131_Screenrecorder-2025-07-10-18-08-33-781.mp4
            String[] parts = url.split("/");
            if (parts.length >= 4) {
                // 跳过协议、域名等，从第4个部分开始拼接
                StringBuilder objectName = new StringBuilder();
                for (int i = 3; i < parts.length; i++) {
                    if (i > 3) objectName.append("/");
                    objectName.append(parts[i]);
                }
                return objectName.toString();
            }
        }
        
        // 如果已经是对象名格式，直接返回
        return url;
    }
} 