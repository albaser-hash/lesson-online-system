package com.lesson.dto;

import lombok.Data;
import java.io.Serializable;
import java.util.List; // 添加List的导入

/**
 * 用户注册
 */
@Data
public class CreateCourseDTO implements Serializable {
    private String courseName;
    private String courseDesc;
    private Integer categoryId;
    private Double price;
    private Double originalPrice;
    private List<String> courseTags; // 使用List类型
    private String coverImage;



}
