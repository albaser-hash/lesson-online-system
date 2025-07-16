package com.lesson.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.sql.Timestamp;

@Data
@TableName("qa_questions")
public class QaQuestion implements Serializable {
    @TableId(type = IdType.AUTO)
    private Integer questionId;
    private Integer userId;
    private String questionTitle;
    private String questionContent;
    private String tags;
    private Integer answerCount;
    private Integer viewCount;
    private Timestamp createTime;
    private Integer bestAnswerId;
} 