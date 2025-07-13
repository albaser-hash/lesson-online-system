package com.lesson.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.sql.Timestamp;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChapterPreviewVO implements Serializable {
    // 章节ID
    private Integer chapterId;
    // 课程ID
    private Integer courseId;
    // 章节名称
    private String chapterName;
    // 排序序号
    private Integer orderNum;
    // 是否免费
    private Integer isFree;
    // 视频时长
    private String videoDuration;
    // 视频大小
    private Long videoSize;
    // 创建时间
    private Timestamp createTime;
    // 内容类型：VIDEO 或 DOCUMENT
    private String contentType;
    // 教师ID
    private Integer teacherId;
} 