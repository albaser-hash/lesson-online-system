package com.lesson.dto;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class UpdateExamDTO {
    private Integer examId;
    private String examName;
    private Date startTime;
    private Date endTime;
    private List<ExamQuestionDTO> questions;
    
    @Data
    public static class ExamQuestionDTO {
        private Integer questionId;
        private String questionType;
        private String questionContent;
        private String options;
        private String correctAnswer;
        private Integer score;
        private String difficulty;
        private String explanation;
    }
} 