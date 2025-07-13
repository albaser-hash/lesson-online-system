package com.lesson.dto;

import lombok.Data;

@Data
public class ToggleRecommendDTO {
    private Integer courseId;
    private Integer score; // 0-100分
} 