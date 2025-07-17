package com.lesson.vo;

import lombok.Data;
import java.io.Serializable;
import java.sql.Timestamp;

@Data
public class QaQuestionDetailVO implements Serializable {
    private Integer questionId;
    private Integer userId;
    private String questionTitle;
    private String questionContent;
    private String tags;
    private Integer answerCount;
    private Integer viewCount;
    private Timestamp createTime;
    private Integer bestAnswerId;

    // 用户信息
    private String userName;
    private String avatar;
} 