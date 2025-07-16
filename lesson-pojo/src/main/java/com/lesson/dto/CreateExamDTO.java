package com.lesson.dto;

import lombok.Data;

import java.sql.Timestamp;
import java.util.List;

@Data
public class CreateExamDTO {
    private Integer courseId;
    private String examName;
    private Timestamp startTime;
    private Timestamp endTime;
    private List<ExamQuestionDTO> questions;
    
    @Data
    public static class ExamQuestionDTO {
        private String questionType;
        private String questionContent;
        private String options;
        private String correctAnswer;
        private Integer score;
        private String difficulty;
        private String explanation;
    }
} 