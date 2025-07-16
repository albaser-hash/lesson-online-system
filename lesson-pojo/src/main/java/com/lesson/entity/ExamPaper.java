package com.lesson.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.sql.Timestamp;

@Data
@TableName("exam_papers")
public class ExamPaper {
    @TableId(type = IdType.AUTO)
    private Integer paperId;
    private Integer userId;
    private Integer examId;
    private String answer; // JSON格式存储答案
    private Integer score;
    private Timestamp submitTime;
    private Timestamp markTime;
    private Integer autoScore; // 自动判分分数，仅客观题
    private Integer finalScore; // 最终分数，老师批改后才有
    private Boolean isReviewed; // 是否已批改，0未批改，1已批改
    private Timestamp reviewTime; // 老师批改时间
    private String subjectiveScores; // 主观题得分，JSON格式，key为questionId，value为得分
} 