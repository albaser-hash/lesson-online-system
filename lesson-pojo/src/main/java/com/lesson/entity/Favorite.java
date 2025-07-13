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
@TableName("favorite")
public class Favorite implements Serializable {
    @TableId(type = IdType.AUTO)//id自增
    // 章节ID，对应表中的 chapter_id 字段，数据类型为 int
    private Integer favoriteId;
    // 课程ID，对应表中的 course_id 字段，数据类型为 int
    private Integer courseId;
    private Integer userId;
    // 创建时间，对应表中的 create_time 字段，数据类型为 timestamp
    private Timestamp createTime;
    private Integer isCancel;
}
