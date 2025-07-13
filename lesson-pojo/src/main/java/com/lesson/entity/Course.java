package com.lesson.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.sql.Timestamp;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("course")
public class Course implements Serializable {
    @TableId(type = IdType.AUTO)//id自增
    // 课程ID，主键、自增
    private Integer courseId;
    // 课程名称，非空
    private String courseName;
    // 课程描述
    private String courseDesc;
    // 分类ID
    private Integer categoryId;
    // 教师ID
    private Integer teacherId;
    // 价格，非空
    private Double price;
    // 原价
    private Double originalPrice;
    // 封面图片
    private String coverImage;
    // 学生数量，默认 0
    private Integer studentCount;
    // 评分，默认 0.0
    private Integer  score;
    // 课程标签
    private String courseTags;
    // 审核状态，枚举类型，默认 'PEND'
    private String auditStatus;
    // 审核消息
    private String auditMessage;
    // 创建时间，默认 CURRENT_TIMESTAMP
    private Timestamp createTime;
}
