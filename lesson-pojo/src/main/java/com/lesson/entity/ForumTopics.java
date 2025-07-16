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
@TableName("forum_topics")
public class ForumTopics implements Serializable {
    @TableId(type = IdType.AUTO)//id自增
    private Integer topicId;
    // 用户 ID
    private Integer userId;
    // 话题标题，varchar 类型
    private String topicTitle;
    // 话题内容，text 类型
    private String topicContent;
    // 话题分类，varchar 类型
    private String topicCategory;
    // 回复数，默认 0
    private Integer replyCount;
    // 创建时间，默认 CURRENT_TIMESTAMP
    private Timestamp createTime;
}
