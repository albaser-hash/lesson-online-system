package com.lesson.vo;

import lombok.Data;

import java.sql.Timestamp;
import java.util.List;

@Data
public class ExamDetailVO {
    private Integer examId;
    private String examName;
    private String courseName;
    private String teacherName;
    private Integer examCount;
    private Timestamp startTime;
    private Timestamp endTime;
    private Timestamp createTime;
    private List<ExamQuestionVO> questions;
    private Boolean isSubmitted;
    private Integer score;
    private Timestamp submitTime;
    private Integer totalScore; // 总分
    private Integer correctCount; // 正确题数
    private Double accuracy; // 正确率（百分比）
    private Boolean isReviewed; // 是否已批改
    private Integer finalScore; // 最终分数
    private Integer autoScore; // 自动判分分数
    
    @Data
    public static class ExamQuestionVO {
        private Integer questionId;
        private String questionType;
        private String questionContent;
        private String options;
        private Integer score;
        private String difficulty;
        private Timestamp createTime;
        private String userAnswer; // 学生答案
        private String correctAnswer; // 正确答案（仅考试结束后显示）
        private Boolean isCorrect; // 是否正确
        private Integer obtainedScore; // 得分
        private String explanation; // 题目解析
    }
} 