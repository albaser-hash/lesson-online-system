package com.lesson.vo;

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
public class ForumTopicsVO implements Serializable {
    private Integer topicId;
    // 创建时间，对应表中的 create_time 字段，数据类型为 timestamp
    private Timestamp publishTime;
}
