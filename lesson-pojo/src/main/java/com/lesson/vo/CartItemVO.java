package com.lesson.vo;

import lombok.Data;

import java.io.Serializable;
import java.sql.Timestamp;

@Data
public class CartItemVO implements Serializable {
    
    private Integer cartItemId;
    private Integer courseId;
    private String courseName;
    private String courseDesc;
    private String coverImage;
    private Double price;
    private Double originalPrice;
    private String teacherName;
    private String categoryName;
    private Timestamp createTime;
} 