package com.lesson.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class PageQueryDTO implements Serializable {

    private int page;

    private int pageSize;

    private String keyword;

    private Integer teacherId;
    
    private Integer categoryId; // 分类ID，用于按分类筛选课程

} 