package com.lesson.dto;

import lombok.Data;

import java.util.Map;

@Data
public class SubmitExamDTO {
    private Integer examId;
    private Map<Integer, String> answers; // questionId -> answer
} 