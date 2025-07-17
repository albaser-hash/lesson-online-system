package com.lesson.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 用户注册
 */
@Data
public class CreateChapterDTO implements Serializable {
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
    // 内容类型：VIDEO 或 DOCUMENT
    private String contentType;



}
