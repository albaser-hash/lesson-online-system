package com.lesson.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;

@Data
@TableName("course_categories")
public class CourseCategory implements Serializable {
    @TableId(type = IdType.AUTO)
    private Integer categoryId;
    private String categoryName;
    private String categoryDesc;
} 