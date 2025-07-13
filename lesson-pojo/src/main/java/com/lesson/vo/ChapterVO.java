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
public class ChapterVO implements Serializable {
    private Integer chapterId;
    private Integer courseId;
    private String chapterName;
    private Integer orderNum;
    private Integer isFree;
    private String videoUrl;
    private String videoDuration;
    private Long videoSize;
    private Timestamp createTime;
    private String contentType;
    private Integer teacherId;
} 