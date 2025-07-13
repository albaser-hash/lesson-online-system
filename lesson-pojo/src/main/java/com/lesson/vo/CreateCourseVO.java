package com.lesson.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateCourseVO implements Serializable {
    private Integer courseId;
    private String courseName;
    private String courseDesc;
    private Long categoryId;
    private Double price;
    private Double originalPrice;
    private List<String> courseTags; // 使用List类型
    private String auditStatues;
    private Timestamp createTime;
    private String coverImage;
}
