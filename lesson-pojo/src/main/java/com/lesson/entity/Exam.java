package com.lesson.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.sql.Timestamp;

@Data
@TableName("exam")
public class Exam {
    @TableId(type = IdType.AUTO)
    private Integer examId;
    private Integer courseId;
    private Integer teacherId;
    private String examName;
    private Integer examCount;
    private Timestamp startTime;
    private Timestamp endTime;
    private Timestamp createTime;
} 