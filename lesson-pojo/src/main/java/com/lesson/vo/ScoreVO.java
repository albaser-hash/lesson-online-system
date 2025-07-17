package com.lesson.vo;

import lombok.Data;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;

/**
 * 用户注册
 */
@Data
public class ScoreVO implements Serializable {
    private String examName;       // 考试名称
    private int score;             // 考试得分
    private Timestamp submitTime;       // 提交时间
    private List<QuestionAnalysis> analysis;  // 试题分析列表
    @Data
    public static class QuestionAnalysis {
        private String questionContent;  // 问题内容
        private String userAnswer;       // 用户答案
        private String correctAnswer;    // 正确答案
        private boolean isCorrect;       // 是否正确
    }




}
