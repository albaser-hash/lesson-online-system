package com.lesson.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 更新章节DTO
 */
@Data
public class UpdateChapterDTO implements Serializable {
    private Integer chapterId;
    private Integer courseId;
    private String chapterName;
    private Integer orderNum;
    private Integer isFree;
    private String videoUrl;
    private String videoDuration;
    private Long videoSize;
    private String contentType;
} 