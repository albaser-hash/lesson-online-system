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
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("exam_papers")
public class Paper implements Serializable {
    @TableId(type = IdType.AUTO)//id自增
    // 课程ID，主键、自增
    private Integer examId;
    // 课程 ID，非空
    private Integer paperId;
    // 教师 ID，非空
    private Integer userId;
    // 考试名称，varchar 类型，长度 100、非空
    private Integer score;
    // 结束时间
    private Timestamp submitTime;
    // 创建时间，默认 CURRENT_TIMESTAMP
    private Timestamp markTime;
    private Integer autoScore; // 自动判分分数，仅客观题
    private Integer finalScore; // 最终分数，老师批改后才有
    private Boolean isReviewed; // 是否已批改，0未批改，1已批改
    private Timestamp reviewTime; // 老师批改时间
    private List<AnswerItem> answer;
    @Data
    public static class AnswerItem {
        private Integer questionId;
        private String userAnswer;
    }

}
