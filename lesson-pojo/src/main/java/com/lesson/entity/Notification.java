package com.lesson.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.sql.Timestamp;

@Data
@TableName("notification")
public class Notification {
    @TableId(type = IdType.AUTO)
    private Integer notificationId;
    private Integer userId;
    private String type; // EXAM, COURSE, SYSTEM
    private String content;
    private Boolean isRead;
    private Timestamp createTime;
    private String link; // 跳转链接
} 