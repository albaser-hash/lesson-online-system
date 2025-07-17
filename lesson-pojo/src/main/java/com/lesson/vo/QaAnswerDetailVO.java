package com.lesson.vo;

import lombok.Data;
import java.io.Serializable;
import java.sql.Timestamp;

@Data
public class QaAnswerDetailVO implements Serializable {
    private Integer answerId;
    private Integer userId;
    private String userName;
    private String avatar;
    private String answerContent;
    private Integer isBest;
    private Timestamp createTime;
} 