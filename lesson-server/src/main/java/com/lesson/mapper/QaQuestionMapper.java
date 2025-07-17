package com.lesson.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lesson.entity.QaQuestion;
import com.lesson.vo.QaQuestionDetailVO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

public interface QaQuestionMapper extends BaseMapper<QaQuestion> {
    @Select("SELECT q.*, COALESCE(NULLIF(u.name, ''), u.user_name) as user_name, u.avatar, COUNT(a.answer_id) AS answerCount, q.best_answer_id as bestAnswerId " +
            "FROM qa_questions q " +
            "LEFT JOIN user u ON q.user_id = u.user_id " +
            "LEFT JOIN qa_answers a ON q.question_id = a.question_id " +
            "WHERE (#{keyword} IS NULL OR q.question_title LIKE CONCAT('%', #{keyword}, '%')) " +
            "GROUP BY q.question_id " +
            "ORDER BY q.create_time DESC")
    Page<QaQuestionDetailVO> selectPageWithUserInfo(Page<QaQuestionDetailVO> page, @Param("keyword") String keyword);

    @Select("SELECT q.*, COALESCE(NULLIF(u.name, ''), u.user_name) as user_name, u.avatar, q.best_answer_id as bestAnswerId " +
            "FROM qa_questions q LEFT JOIN user u ON q.user_id = u.user_id " +
            "WHERE q.question_id = #{id}")
    QaQuestionDetailVO selectDetailById(@Param("id") Integer id);
} 