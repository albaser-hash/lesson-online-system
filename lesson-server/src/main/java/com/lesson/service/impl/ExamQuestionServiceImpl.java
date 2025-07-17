package com.lesson.service.impl;

import com.lesson.entity.ExamQuestion;
import com.lesson.mapper.ExamQuestionMapper;
import com.lesson.service.ExamQuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
public class ExamQuestionServiceImpl implements ExamQuestionService {
    @Autowired
    private ExamQuestionMapper examQuestionMapper;

    @Override
    public List<ExamQuestion> getQuestionsByExamId(Integer examId) {
        return examQuestionMapper.selectByExamId(examId);
    }

    @Override
    @Transactional
    public void saveQuestions(Integer examId, List<ExamQuestion> questions) {
        examQuestionMapper.deleteByExamId(examId);
        if (questions != null && !questions.isEmpty()) {
            examQuestionMapper.batchInsert(examId, questions);
        }
    }

    @Override
    public void clearQuestions(Integer examId) {
        examQuestionMapper.deleteByExamId(examId);
    }
    /**
     * 根据课程ID获取问题列表
     * 此方法首先从数据库中选择所有属于特定课程的问题，然后创建一个Map对象，
     * 以确保返回的问题列表中每个问题都是唯一的（基于问题类型、内容、选项和正确答案的组合）
     *
     * @param courseId 课程ID，用于筛选问题
     * @return 包含唯一问题的列表
     */
    @Override
    public List<ExamQuestion> getQuestionsByCourseId(Integer courseId) {
        // 从数据库中选择所有属于特定课程的问题
        List<ExamQuestion> examQuestions = examQuestionMapper.selectByCourseId(courseId);
        // 创建一个Map对象，用于存储唯一的问题
        Map<String, ExamQuestion> map = new HashMap<>();

        // 遍历问题列表，将每个问题的唯一标识（问题类型+内容+选项+正确答案）作为键，问题对象作为值存入Map
        for (ExamQuestion examQuestion : examQuestions) {
            map.put(examQuestion.getQuestionType() +
                    examQuestion.getQuestionContent() +
                    examQuestion.getOptions() +
                    examQuestion.getCorrectAnswer(), examQuestion);
        }

        // 将Map的值转换为List
        return new ArrayList<>(map.values());
    }
}