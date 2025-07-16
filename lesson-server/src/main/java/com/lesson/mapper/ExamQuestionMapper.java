package com.lesson.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lesson.entity.ExamQuestion;
import com.lesson.vo.ExamDetailVO.ExamQuestionVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExamQuestionMapper extends BaseMapper<ExamQuestion> {
    
    // 获取考试的所有题目
    List<ExamQuestionVO> selectExamQuestions(@Param("examId") Integer examId);
    
    // 获取考试题目（不包含答案，用于考试中）
    List<ExamQuestionVO> selectExamQuestionsForStudent(@Param("examId") Integer examId);

    List<ExamQuestion> selectByExamId(@Param("examId") Integer examId);
    void deleteByExamId(@Param("examId") Integer examId);
    void batchInsert(@Param("examId") Integer examId, @Param("questions") List<ExamQuestion> questions);

    List<ExamQuestion> selectByCourseId(@Param("courseId") Integer courseId);
}
