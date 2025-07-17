package com.lesson.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("course_chapters")
public class Chapter implements Serializable {
    @TableId(type = IdType.AUTO)//id自增
    // 章节ID，对应表中的 chapter_id 字段，数据类型为 int
    private Integer chapterId;
    // 课程ID，对应表中的 course_id 字段，数据类型为 int
    private Integer courseId;
    // 章节名称，对应表中的 chapter_name 字段，数据类型为 varchar，长度 100
    private String chapterName;
    // 排序序号，对应表中的 order_num 字段，数据类型为 int
    private Integer orderNum;
    // 是否免费，对应表中的 is_free 字段，数据类型为 tinyint，默认值 0
    private Integer isFree;
    // 视频URL，对应表中的 video_url 字段，数据类型为 varchar，长度 255
    private String videoUrl;
    // 视频时长，对应表中的 video_duration 字段，数据类型为 time
    private String videoDuration;
    // 视频大小，对应表中的 video_size 字段，数据类型为 bigint
    private Long videoSize;
    // 创建时间，对应表中的 create_time 字段，数据类型为 timestamp
    private Timestamp createTime;
    // 文档分页内容JSON，存储解析后的分页大JSON，字段名对应course_chapters表的doc_json
    private String docJson;
    /**
     * 内容类型：VIDEO 或 DOCUMENT
     */
    private String contentType;
    // 新增：课程教师ID
    private Integer teacherId;
}
