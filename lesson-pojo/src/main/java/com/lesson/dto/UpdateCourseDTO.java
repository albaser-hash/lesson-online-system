package com.lesson.dto;

import lombok.Data;
import java.io.Serializable;
import java.util.List;

/**
 * 更新课程DTO
 */
@Data
public class UpdateCourseDTO implements Serializable {
    private Integer courseId;
    private String courseName;
    private String courseDesc;
    private Integer  categoryId;
    private Double price;
    private Double originalPrice;
    private List<String> courseTags;
    private String coverImage;
    private String auditStatus;
} 