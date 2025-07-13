package com.lesson.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MyCoursesStatsVO implements Serializable {
    private Integer totalCourses;      // 总课程数
    private Integer totalChapters;     // 总章节数
    private Integer totalStudyTime;    // 学习时长（小时）
    private Integer completedCourses;  // 已完成课程数
} 