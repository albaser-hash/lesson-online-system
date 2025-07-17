package com.lesson.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户注册
 */
@Data
public class ForumReplyDTO implements Serializable {
        private Integer replyId;
        private Integer topicId;
        private String replyContent;
        private Integer parentReplyId;
        private Integer replyToUserId;
        private String replyToUserName;



}
