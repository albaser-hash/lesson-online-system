package com.lesson.vo;

import lombok.Data;
import java.io.Serializable;
import java.util.List;

@Data
public class ForumTopicWithRepliesVO implements Serializable {
    private ForumTopicDetailVO topic;
    private List<ForumReplyDetailVO> replies;
} 