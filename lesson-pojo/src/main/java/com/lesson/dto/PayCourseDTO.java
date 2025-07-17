package com.lesson.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户注册
 */
@Data
public class PayCourseDTO implements Serializable {
    private Integer courseId;
    private String payMentMethod;




}
