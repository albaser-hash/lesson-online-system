package com.lesson.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.sql.Timestamp;

@Data
@TableName("qa_answers")
public class QaAnswer implements Serializable {
    @TableId(type = IdType.AUTO)
    private Integer answerId;
    private Integer questionId;
    private Integer userId;
    private String answerContent;
    private Integer isBest;
    private Timestamp createTime;
} 