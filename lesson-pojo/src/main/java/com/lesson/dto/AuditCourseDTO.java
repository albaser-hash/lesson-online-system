package com.lesson.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 课程审核DTO
 */
@Data
public class AuditCourseDTO implements Serializable {
    /**
     * 课程ID
     */
    private Integer courseId;
    
    /**
     * 审核状态：APPROVED-通过，REJECTED-拒绝
     */
    private String auditStatus;
    
    /**
     * 审核消息/拒绝理由
     */
    private String auditMessage;
}
