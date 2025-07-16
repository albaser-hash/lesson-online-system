package com.lesson.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.sql.Timestamp;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("forum_replies")
public class ForumReply implements Serializable {
    @TableId(type = IdType.AUTO)//id自增
    private Integer replyId;
    private Integer topicId;
    // 用户 ID
    private Integer userId;
    // 话题标题，varchar 类型
    private String replyContent;
    private Timestamp createTime;
    // 父回复ID
    private Integer parentReplyId;
    // 被回复用户ID
    private Integer replyToUserId;
    // 被回复用户昵称
    private String replyToUserName;
}
