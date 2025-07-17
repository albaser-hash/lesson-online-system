package com.lesson.service;

import com.lesson.entity.ExamQuestion;
import java.util.List;

public interface ExamQuestionService {
    List<ExamQuestion> getQuestionsByExamId(Integer examId);
    void saveQuestions(Integer examId, List<ExamQuestion> questions);
    void clearQuestions(Integer examId);
    List<ExamQuestion> getQuestionsByCourseId(Integer courseId);
} 