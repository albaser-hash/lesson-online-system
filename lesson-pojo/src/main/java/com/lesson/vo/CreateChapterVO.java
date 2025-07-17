package com.lesson.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateChapterVO implements Serializable {
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
    // 内容类型：VIDEO 或 DOCUMENT
    private String contentType;
}
