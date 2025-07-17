package com.lesson.vo;

import lombok.Data;
import java.io.Serializable;
import java.util.List;

@Data
public class QaQuestionWithAnswersVO extends QaQuestionDetailVO implements Serializable {
    private List<QaAnswerDetailVO> answers;
    private Integer bestAnswerId;
} 