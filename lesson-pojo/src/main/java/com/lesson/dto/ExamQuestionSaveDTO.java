package com.lesson.dto;

import com.lesson.entity.ExamQuestion;
import lombok.Data;
import java.util.List;

@Data
public class ExamQuestionSaveDTO {
    private Integer examId;
    private List<ExamQuestion> questions;
} 