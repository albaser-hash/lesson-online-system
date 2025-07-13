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
public class MyCourseVO implements Serializable {
    private Integer courseId;           // 课程ID
    private String courseName;          // 课程名称
    private String courseDesc;          // 课程描述
    private String categoryName;        // 分类名称
    private String teacherName;         // 教师姓名
    private String coverImage;          // 封面图片
    private Integer progress;           // 学习进度百分比
    private Integer completedChapters;  // 已完成章节数
    private Integer totalChapters;      // 总章节数
    private Integer studyTime;          // 学习时长（分钟）
    private Timestamp lastStudyTime;    // 上次学习时间
    private String status;              // 课程状态：learning/completed
} 