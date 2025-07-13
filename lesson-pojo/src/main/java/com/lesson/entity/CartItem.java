package com.lesson.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.sql.Timestamp;

@Data
@TableName("cart_items")
public class CartItem implements Serializable {
    
    @TableId(type = IdType.AUTO)
    private Integer cartItemId;
    
    private Integer userId;
    
    private Integer courseId;
    
    private Timestamp createTime;
    
    // 非数据库字段，用于显示课程信息
    private String courseName;
    private String courseDesc;
    private String coverImage;
    private Double price;
    private Double originalPrice;
    private String teacherName;
    private String categoryName;
} 