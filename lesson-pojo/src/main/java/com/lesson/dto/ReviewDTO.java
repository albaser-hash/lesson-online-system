package com.lesson.dto;

import lombok.Data;

import java.util.Map;

@Data
public class ReviewDTO {
    private Integer paperId;
    private Map<Integer, Integer> subjectiveScores; // questionId -> score
} 