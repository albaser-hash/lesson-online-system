package com.lesson.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 论坛帖子DTO
 */
@Data
public class ForumTopicsDTO implements Serializable {
        private Integer topicId;
        private String topicTitle;
        private String topicContent;
        private String topicCategory;
}
