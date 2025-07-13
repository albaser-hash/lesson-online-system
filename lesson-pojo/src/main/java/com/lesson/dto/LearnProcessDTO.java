package com.lesson.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户注册
 */
@Data
public class LearnProcessDTO implements Serializable {
    private Integer courseId;
    private Integer chapterId;



}
