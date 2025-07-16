package com.lesson.vo;

import lombok.Data;

import java.io.Serializable;
import java.sql.Timestamp;

@Data
public class ForumTopicDetailVO implements Serializable {
    // 帖子信息
    private Integer topicId;
    private Integer userId;
    private String topicTitle;
    private String topicContent;
    private String topicCategory;
    private Integer replyCount;
    private Timestamp createTime;

    // 用户信息
    private String userName;
    private String avatar;
} 