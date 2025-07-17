package com.lesson.vo;

import lombok.Data;

import java.io.Serializable;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户注册
 */
@Data
public class AttendExamVO implements Serializable {
    private String examName;
    private String courseName;
    private Integer duration; // 考试时长（分钟）
    private Timestamp startTime;
    private Timestamp endTime;
    private List<QuestionDTO> questions;

    @Data
    public static class QuestionDTO {
        private String questionType;//SINGLE_CHOICE, MULTIPLE_CHOICE, JUDGE, TEXT
        private String questionContent;
        private List<String> options;//一个题有四个选项
        private Integer score;
        private String difficulty;//EASY, MEDIUM, HARD
    }

//    @Data
//    public static class OptionDTO {
//        private String optionId;
//        private String optionContent;
//    }




}
