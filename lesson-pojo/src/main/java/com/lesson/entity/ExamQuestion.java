package com.lesson.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.sql.Timestamp;

@Data
@TableName("exam_questions")
public class ExamQuestion {
    @TableId(type = IdType.AUTO)
    private Integer questionId;
    private Integer examId;
    private String questionType; // SINGLE_CHOICE, MULTIPLE_CHOICE, JUDGE, TEXT
    private String questionContent;
    private String options;
    private String correctAnswer;
    private Integer score;
    private String difficulty; // EASY, MEDIUM, HARD
    private String explanation; // 题目解析
    private Timestamp createTime;
} 