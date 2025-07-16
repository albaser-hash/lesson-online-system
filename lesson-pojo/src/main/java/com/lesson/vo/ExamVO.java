package com.lesson.vo;

import lombok.Data;

import java.sql.Timestamp;

@Data
public class ExamVO {
    private Integer examId;
    private Integer courseId;
    private String courseName;
    private Integer teacherId;
    private String teacherName;
    private String examName;
    private Integer examCount;
    private Timestamp startTime;
    private Timestamp endTime;
    private Timestamp createTime;
    private String status; // UPCOMING, ONGOING, FINISHED
    private Boolean hasSubmitted; // 学生是否已提交
    private Integer score; // 学生得分（如果已提交）
} 