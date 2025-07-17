package com.lesson.vo;

import lombok.Data;
import java.io.Serializable;
import java.sql.Timestamp;

@Data
public class ForumReplyDetailVO implements Serializable {
    private Integer replyId;
    private Integer userId;
    private String userName;
    private String avatar;
    private String replyContent;
    private Timestamp createTime;
    private Integer parentReplyId;
    private Integer replyToUserId;
    private String replyToUserName;
    private java.util.List<ForumReplyDetailVO> children;
} 