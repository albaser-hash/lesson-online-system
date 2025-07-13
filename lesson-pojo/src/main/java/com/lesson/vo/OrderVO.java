package com.lesson.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;

@Data
public class OrderVO implements Serializable {
    
    private String orderId;
    private Integer userId;
    private Integer courseId;
    private String courseName;
    private String coverImage;
    private String teacherName;
    private String categoryName;
    private String paymentMethod;
    private BigDecimal orderAmount;
    private String orderStatus;
    private Timestamp createTime;
    private Timestamp payTime;
} 