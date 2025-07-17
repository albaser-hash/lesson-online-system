package com.lesson.service;

import com.lesson.dto.PageQueryDTO;
import com.lesson.entity.QaQuestion;
import com.lesson.result.PageResult;
import com.lesson.vo.QaQuestionDetailVO;
import com.lesson.vo.QaQuestionWithAnswersVO;

public interface QaQuestionService {
    void add(QaQuestion qaQuestion);
    void update(QaQuestion qaQuestion);
    void delete(Integer questionId);
    QaQuestion getById(Integer questionId);
    PageResult list(PageQueryDTO pageQueryDTO);

    /**
     * 分页带用户信息
     */
    PageResult listWithUserInfo(PageQueryDTO pageQueryDTO);

    /**
     * 详情带用户信息
     */
    QaQuestionDetailVO getDetailById(Integer questionId);

    /**
     * 详情带所有回答
     */
    QaQuestionWithAnswersVO getFullDetailById(Integer questionId);

    /**
     * 设置最佳回答
     */
    void setBestAnswer(Integer questionId, Integer answerId);
} 