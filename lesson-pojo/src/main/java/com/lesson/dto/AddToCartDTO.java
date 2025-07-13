package com.lesson.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class AddToCartDTO implements Serializable {
    
    private Integer courseId;
} 