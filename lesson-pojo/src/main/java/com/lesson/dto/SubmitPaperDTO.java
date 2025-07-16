package com.lesson.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 用户注册
 */
@Data
public class SubmitPaperDTO implements Serializable {
    private Integer examId;
    private List<AnswerItem> answers;

    public static class AnswerItem {
        private Integer questionId;
        private String userAnswer;
    }




}
