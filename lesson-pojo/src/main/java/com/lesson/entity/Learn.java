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
@TableName("learning_progress")
public class Learn implements Serializable {
    @TableId(type = IdType.AUTO)
    private Integer progressId;
    /**
     * 用户ID
     * 对应数据库字段：user_id
     */
    private Integer userId;

    /**
     * 课程ID
     * 对应数据库字段：course_id
     */
    private Integer courseId;

    /**
     * 章节ID
     * 对应数据库字段：chapter_id
     */
    private Integer chapterId;

    /**
     * 状态（可根据业务定义：如 0-未看完、1-已看完 等）
     * 对应数据库字段：status
     */
    private Integer status;

    /**
     * 最后观看时间
     * 对应数据库字段：last_watch_time
     */
    private Timestamp lastWatchTime;
}
